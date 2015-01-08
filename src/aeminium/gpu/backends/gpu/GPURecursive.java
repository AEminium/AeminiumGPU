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

	public static final int DEFAULT_SPLIT_VALUE = 512;

	
	public T output;
	public RecursiveStrategy<R, T> strategy;
	PList<R> starts;
	PList<Boolean> results;
	Stack<Pair> stack = new Stack<Pair>();
	boolean isDone;
	
	protected CLBuffer<?> sbuffer;
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
		starts.set(starts.size(), end);
		return starts.size() - 1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {
		
		// initial data;
		int workUnits = prepareReadBuffers(strategy.getStart(), strategy.getEnd(), DEFAULT_SPLIT_VALUE, 0);
		
		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx, "Integer", workUnits);
		abuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getSeed().getClass().getSimpleName(), workUnits);
		CLEvent[] eventsArr = new CLEvent[1];
		
		int processNext = -1;
		while (!isDone) {
			sbuffer = BufferHelper.createInputBufferFor(ctx, starts, starts.size());
			
			synchronized (kernel) {
				kernel.setArgs(sbuffer, abuffer, rbuffer);
				setExtraDataArgs(kernel);
				
				eventsArr[0] = kernel.enqueueNDRange(q,
						new int[] { workUnits }, eventsArr);
			}
			if (System.getenv("DEBUG") != null) {
				BufferHelper.debugBuffers(ctx, q, "results", rbuffer, 10, eventsArr[0], "Integer");
				BufferHelper.debugBuffers(ctx, q, "acc", abuffer, 10, eventsArr[0], "Double");
			}
			
			PList<Integer> rs = (PList<Integer>) BufferHelper.extractFromBuffer(rbuffer, q,
					eventsArr[0], "Integer", workUnits);
			PList<T> accs = (PList<T>) BufferHelper.extractFromBuffer(abuffer, q, eventsArr[0], strategy.getSeed().getClass().getSimpleName(), workUnits);
			sbuffer.release();
			int done=0;
			for (int i=0; i<workUnits; i++) {
				if (rs.get(i) == 1) {
					output = strategy.combine(output, accs.get(i));
					done++;
				} else {
					stack.push(new Pair(starts.get(i), starts.get(i+1)));
				}
			}
			if (System.getenv("DEBUG") != null) {
				System.out.println(done + ", q: " + stack.size() + ", rec: " + processNext);
			}
			
			if (stack.isEmpty()) {
				isDone = true;
			} else {
				if (processNext == -1) {
					processNext = stack.size() / 2;
				} else if (done == workUnits) {
					processNext *= 2;
				} else {
					processNext /= 2;
					if (processNext < 1) processNext = 1;
				}
				int pNext = (processNext > stack.size()) ? stack.size() : processNext;
				int steps = DEFAULT_SPLIT_VALUE / pNext;
				for (int k=0; k<pNext; k++) {
					Pair p = stack.pop();	
					workUnits = prepareReadBuffers(p.s, p.e, steps, k * steps);
				}
			}
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
