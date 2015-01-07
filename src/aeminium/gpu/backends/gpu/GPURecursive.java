package aeminium.gpu.backends.gpu;

import java.util.LinkedList;
import java.util.Queue;

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
	public static final int MAX_SPLITS = 4;

	
	public T output;
	public RecursiveStrategy<R, T> strategy;
	PList<R> starts;
	PList<R> ends;
	PList<Boolean> results;
	Queue<Pair> queue = new LinkedList<Pair>();
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

	
	public int prepareReadBuffers(R st, R end) {
		starts = createEmptyList();
		strategy.split(starts, 0, st, end, DEFAULT_SPLIT_VALUE);
		ends = starts.subList(1, starts.size());
		ends.set(starts.size() - 1, end);
		return starts.size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {
		
		// initial data;
		int workUnits = prepareReadBuffers(strategy.getStart(), strategy.getEnd());
		
		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx, "Integer", workUnits);
		abuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getSeed().getClass().getSimpleName(), workUnits);
		CLEvent[] eventsArr = new CLEvent[1];
		
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
			for (int i=0; i<workUnits; i++) {
				if (rs.get(i) == 1) {
					output = strategy.combine(output, accs.get(i));
				} else {
					queue.add(new Pair(starts.get(i), ends.get(i)));
				}
			}
			if (queue.isEmpty()) {
				isDone = true;
			} else {
				Pair p = queue.poll();
				workUnits = prepareReadBuffers(p.s, p.e);
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
