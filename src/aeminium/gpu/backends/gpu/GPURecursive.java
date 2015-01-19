package aeminium.gpu.backends.gpu;

import java.util.Stack;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.RecursiveCodeGen;
import aeminium.gpu.backends.gpu.generators.RecursiveTemplateSource;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.AbstractRecursiveStrategy;
import aeminium.gpu.operations.functions.Range2D;
import aeminium.gpu.utils.Quad;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPURecursive<R extends Number, R2, T> extends GPUGenericKernel implements RecursiveTemplateSource<R, R2, T> {

	public static final int DEFAULT_SPLIT_VALUE = 512;

	
	public T output;
	public AbstractRecursiveStrategy<R, R2, T> strategy;
	PList<R> starts;
	PList<R> ends;
	PList<R2> tops;
	PList<R2> bottoms;
	PList<Boolean> results;
	Stack<Quad<R, R2>> stack = new Stack<Quad<R, R2>>();
	boolean isDone;
	
	protected CLBuffer<?> sbuffer;
	protected CLBuffer<?> ebuffer;
	protected CLBuffer<?> tbuffer = null;
	protected CLBuffer<?> bbuffer = null;
	protected CLBuffer<?> abuffer;
	protected CLBuffer<Integer> rbuffer;
	
	private RecursiveCodeGen gen;
	
	public GPURecursive(AbstractRecursiveStrategy<R, R2, T> recursiveStrategy) {
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

	
	public int prepareReadBuffers(R st, R end, R2 top, R2 bottom, int splits, int index) {
		Range2D<R, R2> ranges = strategy.split(st, end, top, bottom, splits);
		
		starts.extendAt(index, ranges.starts);
		ends.extendAt(index, ranges.ends);
		if (ranges.tops != null) tops.extendAt(index, ranges.tops);
		if (ranges.bottoms != null) bottoms.extendAt(index, ranges.bottoms);
		return starts.size() - 1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {
		
		// initial data;
		int workUnits = prepareReadBuffers(strategy.getStart(), strategy.getEnd(), strategy.getTop(), strategy.getBottom(), DEFAULT_SPLIT_VALUE, 0);
		
		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx, "Integer", workUnits);
		abuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getSeed().getClass().getSimpleName(), workUnits);
		CLEvent[] eventsArr = new CLEvent[1];
		
		int processNext = -1;
		while (!isDone) {
			sbuffer = BufferHelper.createInputBufferFor(ctx, starts, starts.size());
			ebuffer = BufferHelper.createInputBufferFor(ctx, ends, ends.size());
			if (tops != null) tbuffer = BufferHelper.createInputBufferFor(ctx, tops, tops.size());
			if (bottoms != null) bbuffer = BufferHelper.createInputBufferFor(ctx, bottoms, bottoms.size());
			
			synchronized (kernel) {
				kernel.setArgs(sbuffer, ebuffer, tbuffer, bbuffer, abuffer, rbuffer);
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
					stack.push(new Quad<R, R2>(starts.get(i), ends.get(i), tops.get(i), bottoms.get(i)));
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
					Quad<R, R2> p = stack.pop();	
					workUnits = prepareReadBuffers(p.s, p.e, p.t, p.b, steps, k * steps);
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
	public String getR2Type() {
		return BufferHelper.getCLTypeOf(strategy.getTop().getClass().getSimpleName());
	}

	@Override
	public String getTType() {
		return BufferHelper.getCLTypeOf(strategy.getSeed().getClass().getSimpleName());
	}

	@Override
	public AbstractRecursiveStrategy<R, R2, T> getRecursiveStrategy() {
		return strategy;
	}
	
}
