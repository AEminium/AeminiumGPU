package aeminium.gpu.operations;

import org.bridj.Pointer;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.collections.PCollection;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lazyness.LazyEvaluator;
import aeminium.gpu.collections.lazyness.LazyPList;
import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.executables.GenericProgram;
import aeminium.gpu.executables.Program;
import aeminium.gpu.operations.deciders.OpenCLDecider;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;
import aeminium.gpu.operations.generator.MapCodeGen;
import aeminium.gpu.operations.mergers.MapToMapMerger;
import aeminium.gpu.operations.mergers.MapToReduceMerger;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class Map<I,O> extends GenericProgram implements Program {
	
	protected PCollection<I> input;
	private PList<O> output;
	protected LambdaMapper<I,O> mapFun;
	
	protected CLBuffer<?> inbuffer;
	private CLBuffer<?> outbuffer;
	
	private MapCodeGen gen;
	
	// Constructors
	
	public Map(LambdaMapper<I, O> mapFun2, PList<I> list, GPUDevice dev) {
		this(mapFun2, list, "", dev);
	}
	
	public Map(LambdaMapper<I, O> mapFun, PList<I> list, String other, GPUDevice dev) {
		this.device = dev;
		this.input = list;
		this.mapFun = mapFun;
		this.setOtherSources(other);
		gen = new MapCodeGen(this);
		if (list instanceof Range) {
			gen.setRange(true);
		}
	}
	
	// Pipeline
	
	public void execute() {
		if (System.getenv("BENCH") != null) {
			boolean isGpu = willRunOnGPU();
			long startT = System.nanoTime();
			run();
			long gpuT = System.nanoTime() - startT;
			
			startT = System.nanoTime();
			cpuExecution();
			long cpuT = System.nanoTime() - startT;
			System.out.println("GPUreal: " + gpuT);
			System.out.println("CPUreal: " + cpuT);
			
			if ( isGpu == (gpuT < cpuT) ) {
				System.out.println("GPUvsCPU: right");
			} else {
				System.out.println("GPUvsCPU: wrong");
			}
			return;
		}
		// TODO: Consider size: 0
		if (willRunOnGPU()) {
			run();
		} else {
			cpuExecution();
		}
	}
	
	private boolean willRunOnGPU() {
		return OpenCLDecider.useGPU(input.size(), mapFun.getSource(), mapFun.getSourceComplexity());
	}

	@SuppressWarnings("unchecked")
	public void cpuExecution() {
		output = (PList<O>) CollectionFactory.listFromType(getOutputType());
		for (int i=0; i< input.size(); i++ ) {
			output.add(mapFun.map(input.get(i)));
		}
	}
	
	
	@Override
	protected String getSource() {
		return gen.getMapKernelSource();
	}
	
	public String getMapOpenCLSource() {
		return gen.getMapLambdaSource();
	}
	
	public String getMapOpenCLName() {
		return gen.getMapLambdaName();
	}
	
	@Override
	public void prepareBuffers(CLContext ctx) {
		if (input instanceof Range) {
			Pointer<Integer> ptr = Pointer.allocateInts(1).order(ctx.getByteOrder());
			inbuffer = ctx.createBuffer(CLMem.Usage.Input, ptr, false);
		} else {
			inbuffer = BufferHelper.createInputBufferFor(ctx, input);
		}
		outbuffer = BufferHelper.createOutputBufferFor(ctx, getOutputType(), input.size());
	}

	@Override
	public void execute(CLContext ctx, CLQueue q) {
		synchronized (kernel) {
		    // setArgs will throw an exception at runtime if the types / sizes of the arguments are incorrect
			kernel.setArgs(inbuffer, outbuffer);
		    
		    // Ask for 1-dimensional execution of length dataSize, with auto choice of local workgroup size :
		    kernelCompletion = kernel.enqueueNDRange(q, new int[] { input.size() }, new CLEvent[] {});
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		output = (PList<O>) BufferHelper.extractFromBuffer(outbuffer, q, kernelCompletion, getOutputType(), input.size());
	}

	
	@Override
	public void release() {
		super.release();
		this.inbuffer.release();
		this.outbuffer.release();
	}

	// Output
	
	public PList<O> getOutput() {
		final Map<I,O> innerMap = this;
		
		// Lazy return
		LazyEvaluator<O> operation = new LazyEvaluator<O>() {

			@Override
			public PList<O> evaluate() {
				innerMap.execute();
				return output;
			}

			@Override
			public Class<?> getType() {
				return BufferHelper.getClassOf(getOutputType());
			}

			@Override
			public <K> boolean canMergeWithMap(LambdaMapper<O, K> mapFun) {
				return true;
			}

			@Override
			public <K> PList<K> mergeWithMap(Map<O, K> mapOp) {
				MapToMapMerger<I,O,K> merger = new MapToMapMerger<I,O,K>(innerMap, mapOp, input);
				return merger.getOutput();
			}

			@Override
			public boolean canMergeWithReduce(LambdaReducer<O> reduceFun) {
				return true;
			}

			@Override
			public O mergeWithReducer(Reduce<O> reduceOp) {
				MapToReduceMerger<I,O> merger = new MapToReduceMerger<I,O>(innerMap, reduceOp, input);
				return merger.getOutput();
			}
			
		};
		return new LazyPList<O>(operation, input.size());
	}
	
	
	// Utils
	
	public String getInputType() {
		return input.getType().getSimpleName().toString();
	}
	
	public String getOutputType() {
		Class<?> klass = mapFun.getClass();
		try {
			return klass.getMethod("map", input.getType()).getReturnType().getSimpleName().toString();
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			// Java sucks
			// TODO: Compiler can give us this information.
			System.out.println("AeminiumGPU Runtime does not support generic types on Lambdas.");
			System.exit(0);
			return null;
		}
	}
	
	public int getOutputSize() {
		return input.size();
	}
	

	// Getters and Setters
	
	public void setOutput(PList<O> output) {
		this.output = output;
	}

	public LambdaMapper<I, O> getMapFun() {
		return mapFun;
	}

	public void setMapFun(LambdaMapper<I, O> mapFun) {
		this.mapFun = mapFun;
	}
	
	@Override
	public String getKernelName() {
		return gen.getMapKernelName();
	}
}
