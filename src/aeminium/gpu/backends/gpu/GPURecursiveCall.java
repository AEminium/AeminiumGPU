package aeminium.gpu.backends.gpu;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.RecursiveCallCodeGen;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.BinaryRecursiveStrategy;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPURecursiveCall<R, A> extends GPUGenericKernel {

	public PList<A> args;
	public PList<A> argsNext;
	public static final int DEFAULT_SPAWN = 8;
	public static final int MAX_ITEMS = 512;
	public R output;
	public BinaryRecursiveStrategy<R, A> strategy;
	boolean isDone;

	protected CLBuffer<?> argbuffer;
	protected CLBuffer<?> accbuffer;
	protected CLBuffer<Integer> rbuffer;

	private RecursiveCallCodeGen gen;

	public GPURecursiveCall(BinaryRecursiveStrategy<R, A> recursiveStrategy) {
		strategy = recursiveStrategy;
		gen = new RecursiveCallCodeGen(this);
		output = strategy.getSeed();

		otherData = OtherData.extractOtherData(recursiveStrategy);
		gen.setOtherData(otherData);
	}

	public void setArgs(PList<A> args) {
		this.args = args;
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		super.prepareBuffers(ctx);
		argsNext = args.subList(0, 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {

		CLEvent[] eventsArr = new CLEvent[1];

		args = args.subList(start, end);
		int workUnits = args.length();
		int bufferSize = workUnits * DEFAULT_SPAWN;

		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx,
				"Integer", MAX_ITEMS * DEFAULT_SPAWN);
		accbuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getSeed()
				.getClass().getSimpleName(), MAX_ITEMS);

		int counter = 0;
		while (!isDone) {
			
			workUnits = args.length(); //end - start; // Using Limits from Decider
			bufferSize = workUnits * DEFAULT_SPAWN;
			
			argbuffer = BufferHelper.createInputOutputBufferFor(ctx, args,
					bufferSize);

			synchronized (kernel) {
				kernel.setArgs(counter, workUnits, rbuffer, accbuffer, argbuffer);
				setExtraDataArgs(5, kernel);

				eventsArr[0] = kernel.enqueueNDRange(q,
						new int[] { workUnits }, eventsArr);
			}
			counter++;
			
			PList<Integer> rs = (PList<Integer>) BufferHelper
					.extractFromBuffer(rbuffer, q, eventsArr[0], "Integer",
							bufferSize);
			PList<A> argsBack = (PList<A>) BufferHelper.extractFromBuffer(
					argbuffer, q, eventsArr[0], bufferSize, args);
			
			for (int i = 0; i < bufferSize; i++) {
				if (rs.get(i) == 0){
					argsNext.add(argsBack.get(i));
				}
			}
			
			if (System.getenv("DEBUG") != null) {
				System.out.println("Left: " + argsNext.size());
			}
			
			if (argsNext.isEmpty()) {
				isDone = true;
			} else {
				if (argsNext.size() < MAX_ITEMS) {
					args = argsNext;
					argsNext = args.subList(0, 0);
				} else {
					args = argsNext.subList(0, MAX_ITEMS);
					argsNext = argsNext.subList(MAX_ITEMS, argsNext.size());
				}
			}
		}
		
		PList<R> accs = (PList<R>) BufferHelper.extractFromBuffer(
				accbuffer, q, eventsArr[0], strategy.getSeed().getClass()
						.getSimpleName(), workUnits);
		for (int i = 0; i < MAX_ITEMS; i++) {
			output = strategy.combine(output, accs.get(i));
		}
	}
	
	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
	}

	@Override
	public String getSource() {
		return gen.getRecursiveKernelSource();
	}

	@Override
	public String getKernelName() {
		return gen.getRecursiveKernelName();
	}
	
	public Integer getStackSize() {
		return DEFAULT_SPAWN; 
	}

	public R getOutput() {
		return output;
	}

	public String getRType() {
		return BufferHelper.getCLTypeOf(strategy.getSeed().getClass()
				.getSimpleName());
	}

	public String getAType() {
		return BufferHelper.getCLTypeOf(strategy.getArgument().getClass()
				.getSimpleName());
	}

	public BinaryRecursiveStrategy<R, A> getRecursiveStrategy() {
		return strategy;
	}


}
