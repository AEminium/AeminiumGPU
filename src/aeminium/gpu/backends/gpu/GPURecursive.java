package aeminium.gpu.backends.gpu;

import java.util.Stack;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.RecursiveCodeGen;
import aeminium.gpu.backends.gpu.generators.RecursiveTemplateSource;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.RecursiveStrategy;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPURecursive<R extends Number, T> extends GPUGenericKernel implements RecursiveTemplateSource<R,T> {

	public static final int DEFAULT_SPLIT_VALUE = 1024;

	
	public T output;
	public RecursiveStrategy<R, T> strategy;
	PList<R> starts;
	PList<R> ends;
	PList<Boolean> results;
	Stack<Pair> stack = new Stack<Pair>();
	boolean isDone;
	
	protected CLBuffer<?> sbuffer;
	protected CLBuffer<?> ebuffer;
	protected CLBuffer<?> abuffer;
	protected CLBuffer<Integer> rbuffer;
	
	private RecursiveCodeGen gen;
	
	public GPURecursive(RecursiveStrategy<R, T> recursiveStrategy) {
		strategy = recursiveStrategy;
		gen = new RecursiveCodeGen(this);
		output = strategy.getSeed();
		
		otherData = OtherData.extractOtherData(recursiveStrategy);
		gen.setOtherData(otherData);
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		super.prepareBuffers(ctx);
	}

	
	public int prepareReadBuffers(R st, R end, int splits, int index) {
		starts = createEmptyList();
		strategy.split(starts, index, st, end, splits);
		ends = starts.subList(1, starts.size());
		ends.set(starts.size() - 1, end);
		return starts.size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {
		
		// initial data;
		int workUnits = prepareReadBuffers(strategy.getStart(), strategy.getEnd(), DEFAULT_SPLIT_VALUE, 0);
		
		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx, "Integer", workUnits);
		abuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getSeed().getClass().getSimpleName(), workUnits);
		CLEvent[] eventsArr = new CLEvent[1];
		
		int iter = 0;
		int processNext = 1;
		while (!isDone) {
			sbuffer = BufferHelper.createInputBufferFor(ctx, starts, starts.size());
			ebuffer = BufferHelper.createInputBufferFor(ctx, ends, ends.size());
			
			synchronized (kernel) {
				kernel.setArgs(sbuffer, ebuffer, abuffer, rbuffer);
				setExtraDataArgs(kernel);
				
				eventsArr[0] = kernel.enqueueNDRange(q,
						new int[] { workUnits }, eventsArr);
			}
			
			PList<Integer> rs = (PList<Integer>) BufferHelper.extractFromBuffer(rbuffer, q,
					eventsArr[0], "Integer", workUnits);
			PList<T> accs = (PList<T>) BufferHelper.extractFromBuffer(abuffer, q, eventsArr[0], strategy.getSeed().getClass().getSimpleName(), workUnits);
			sbuffer.release();
			ebuffer.release();
			int done=0;
			for (int i=0; i<workUnits; i++) {
				if (rs.get(i) == 1) {
					output = strategy.combine(output, accs.get(i));
					done++;
				} else {
					stack.push(new Pair(starts.get(i), ends.get(i)));
				}
			}
			if (System.getenv("DEBUG") != null) {
				if (iter % 1000 == 0) {
					System.out.println(done + ", q: " + stack.size() + ", rec: " + processNext);
				}
			}
			if (stack.isEmpty()) {
				isDone = true;
			} else {
				if (done == workUnits) {
					processNext += 1;
				} else {
					processNext -= 1;
					if (processNext < 1) processNext = 1;
				}
				Pair p = stack.pop();
				workUnits = 0;
				int steps = DEFAULT_SPLIT_VALUE / processNext;
				System.out.println("n: " + processNext + " steps:" + steps);
				for (int k=0; k<processNext; k++) {
					workUnits += prepareReadBuffers(p.s, p.e, steps, k * steps);
				}
			}
			iter ++;
		}
		rbuffer.release();
		abuffer.release();
	}

	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		// Nothing to do!
	}

	@Override
	public String getSource() {
		return gen.getRecursiveKernelSource();
	}

	@Override
	public String getKernelName() {
		return gen.getRecursiveKernelName();
	}

	public T getOutput() {
		return output;
	}
	
	@SuppressWarnings("unchecked")
	public PList<R> createEmptyList() {
		return (PList<R>) CollectionFactory.listFromType(strategy.getStart().getClass().getSimpleName());
	}
	
	class Pair {
		public R s;
		public R e;
		
		public Pair(R s, R e) {
			this.s = s;
			this.e = e;
		}
	}

	@Override
	public String getRType() {
		return BufferHelper.getCLTypeOf(strategy.getStart().getClass().getSimpleName());
	}

	@Override
	public String getTType() {
		return BufferHelper.getCLTypeOf(strategy.getSeed().getClass().getSimpleName());
	}

	@Override
	public RecursiveStrategy<R, T> getRecursiveStrategy() {
		return strategy;
	}
	
}
