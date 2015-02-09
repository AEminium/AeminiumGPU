package aeminium.gpu.backends.gpu;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.RecursiveCodeGen;
import aeminium.gpu.backends.gpu.generators.RecursiveTemplateSource;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.Range2D;
import aeminium.gpu.operations.functions.Recursive2DStrategy;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPURangedRecursiveCall<R extends Number, R2, T> extends GPUGenericKernel
		implements RecursiveTemplateSource<R, R2, T> {

	public static int MAX_WORKERS = 4096;
	private static int KERNEL_RECURSION_LIMIT = 1024;
	
	static {
		if (System.getenv("WORKERS") != null) MAX_WORKERS = Integer.parseInt(System.getenv("WORKERS"));
		if (System.getenv("RECs") != null) KERNEL_RECURSION_LIMIT = Integer.parseInt(System.getenv("RECS"));
	}

	public T output;
	public Recursive2DStrategy<R, R2, T> strategy;
	PList<R> starts;
	PList<R> ends;
	PList<R2> tops;
	PList<R2> bottoms;

	protected CLBuffer<?> sbuffer;
	protected CLBuffer<?> ebuffer;
	protected CLBuffer<?> abuffer;
	protected CLBuffer<?> stepbuffer;
	protected CLBuffer<Integer> rbuffer;
	
	protected String tType;
	protected String rType;
	
	protected int workUnits = 0;

	private RecursiveCodeGen<R, R2, T> gen;

	public GPURangedRecursiveCall(Recursive2DStrategy<R, R2, T> recursiveStrategy) {
		strategy = recursiveStrategy;
		gen = new RecursiveCodeGen<R, R2, T>(this);
		output = strategy.getSeed();

		otherData = OtherData.extractOtherData(recursiveStrategy);
		gen.setOtherData(otherData);
		
		tType = strategy.getSeed().getClass().getSimpleName();
		rType = strategy.getStart().getClass().getSimpleName();
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		super.prepareBuffers(ctx);
		createEmptyLists();
		Range2D<R, R2> range = strategy.split(strategy.getStart(), strategy.getEnd(), null, null, MAX_WORKERS);
		extendRange(range);
	}
	
	private void extendRange(Range2D<R, R2> range) {
		starts.extend(range.starts);
		ends.extend(range.ends);
		workUnits += range.size();
	}

	public void copyRangeBuffers(CLContext ctx) {
		sbuffer = BufferHelper.createInputOutputBufferFor(ctx, starts, starts.size());
		ebuffer = BufferHelper.createInputBufferFor(ctx, ends, ends.size());
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {
		CLEvent[] eventsArr = new CLEvent[1];
		
		int itemsLeft = workUnits;
		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx, "Integer", MAX_WORKERS);
		abuffer = BufferHelper.createOutputBufferFor(ctx, tType, MAX_WORKERS);
		stepbuffer = BufferHelper.createOutputBufferFor(ctx, rType, MAX_WORKERS);
		int global_counter = 0;
		while (itemsLeft > 0) {
			copyRangeBuffers(ctx);
			synchronized(kernel) {
				kernel.setArgs(workUnits, sbuffer, ebuffer, abuffer, rbuffer, stepbuffer, global_counter++);
				setExtraDataArgs(5, kernel);
				eventsArr[0] = kernel.enqueueNDRange(q, new int[] { MAX_WORKERS }, eventsArr);
			}
			int[] rs = rbuffer.read(q, eventsArr[0]).getInts();
			PList<T> accs = (PList<T>) BufferHelper.extractFromBuffer(abuffer, q, eventsArr[0], tType, workUnits);
			itemsLeft = 0;
			for (int i=workUnits-1; i>=0; i--) {
				if (System.getenv("DEBUG") != null && System.getenv("DEBUG").equals("2")) System.out.print(rs[i] + "|" + starts.get(i) + "|" + ends.get(i) + "|" + accs.get(i) + ", ");
				
				if (rs[i] == 2) {
					removeRangeIndex(i);
					output = strategy.combine(output, accs.get(i));
				} else {
					itemsLeft++;
				}
			}
			
			if (System.getenv("DEBUG") != null && itemsLeft == 0) System.out.println(" No items left ");
			
			while (itemsLeft < MAX_WORKERS && itemsLeft > 0) {
				int removeIndex = 0;
				int diff = MAX_WORKERS-itemsLeft+1;
				Range2D<R, R2> range = strategy.split(starts.get(removeIndex), ends.get(removeIndex), null, null, diff);
				removeRangeIndex(removeIndex);
				extendRange(range);
				itemsLeft += range.size()-1;
			}
			workUnits = starts.size();
		}
		
				
	}

	private void removeRangeIndex(int i) {
		ends.remove(i);
		starts.remove(i);
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
	public void createEmptyLists() {
		starts = (PList<R>) CollectionFactory.listFromType(rType);
		ends = (PList<R>) CollectionFactory.listFromType(rType);
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
		return BufferHelper.getCLTypeOf(strategy.getStart().getClass()
				.getSimpleName());
	}

	@Override
	public String getR2Type() {
		if (strategy.getTop() == null)
			return "void*";
		return BufferHelper.getCLTypeOf(strategy.getTop().getClass()
				.getSimpleName());
	}

	@Override
	public String getTType() {
		return BufferHelper.getCLTypeOf(strategy.getSeed().getClass()
				.getSimpleName());
	}

	@Override
	public Recursive2DStrategy<R, R2, T> getRecursiveStrategy() {
		return strategy;
	}

	@Override
	public int getRecursionLimit() {
		return KERNEL_RECURSION_LIMIT;
	}
	
	public int getNumWorkers() {
		return MAX_WORKERS;
	}

}
