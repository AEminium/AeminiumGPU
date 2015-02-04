package aeminium.gpu.backends.gpu;

import java.util.Stack;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.RecursiveCodeGen;
import aeminium.gpu.backends.gpu.generators.RecursiveTemplateSource;
import aeminium.gpu.collections.factories.CollectionFactory;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.Range2D;
import aeminium.gpu.operations.functions.Recursive2DStrategy;
import aeminium.gpu.utils.Quad;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPURangedRecursiveCall<R extends Number, R2, T> extends GPUGenericKernel
		implements RecursiveTemplateSource<R, R2, T> {

	public static final int NUM_WORKERS = 128;
	private static final int RECURSION_LIMIT = 512;

	public T output;
	public Recursive2DStrategy<R, R2, T> strategy;
	PList<R> starts;
	PList<R> ends;
	PList<R2> tops;
	PList<R2> bottoms;
	PList<Boolean> results;
	Stack<Quad<R, R2>> stack = new Stack<Quad<R, R2>>();
	
	int maxWorkUnits = 0;
	boolean isDone;
	boolean is2D = true;

	protected CLBuffer<?> sbuffer;
	protected CLBuffer<?> pbuffer;
	protected CLBuffer<?> ebuffer;
	protected CLBuffer<?> tbuffer = null;
	protected CLBuffer<?> bbuffer = null;
	protected CLBuffer<?> abuffer;
	protected CLBuffer<Integer> rbuffer;

	private RecursiveCodeGen<R, R2, T> gen;

	public GPURangedRecursiveCall(Recursive2DStrategy<R, R2, T> recursiveStrategy) {
		strategy = recursiveStrategy;
		gen = new RecursiveCodeGen<R, R2, T>(this);
		output = strategy.getSeed();
		
		if (strategy.getTop() == null) is2D = false;

		otherData = OtherData.extractOtherData(recursiveStrategy);
		gen.setOtherData(otherData);
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		super.prepareBuffers(ctx);
		createEmptyLists();
	}

	public int prepareReadBuffers(R st, R end, R2 top, R2 bottom, int splits) {
		Range2D<R, R2> ranges = strategy.split(st, end, top, bottom, splits);
		starts = ranges.starts;
		ends = ranges.ends;
		if (is2D) {
			tops = ranges.tops;
			bottoms = ranges.bottoms;
		}
		return ranges.size();
	}

	
	public void copyRangeBuffers(CLContext ctx) {
		sbuffer = BufferHelper.createInputOutputBufferFor(ctx, starts,
				starts.size());
		ebuffer = BufferHelper.createInputBufferFor(ctx, ends, ends.size());
		if (is2D) {
			tbuffer = BufferHelper.createInputOutputBufferFor(ctx, tops,
					tops.size());
			bbuffer = BufferHelper.createInputBufferFor(ctx, bottoms,
					bottoms.size());
		}
	}
	
	public int extendFirst(int size) {
		Range2D<R, R2> ranges;
		if (is2D) {
			ranges = strategy.split(starts.remove(0), ends.remove(0), tops.remove(0), bottoms.remove(0), size+1);
		} else {
			ranges = strategy.split(starts.remove(0), ends.remove(0), null, null, size+1);
		} 
		starts.extend(ranges.starts);
		ends.extend(ranges.ends);
		if (is2D) {
			tops.extend(ranges.tops);
			bottoms.extend(ranges.bottoms);
		}
		return ranges.size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {
		CLEvent[] eventsArr = new CLEvent[1];
		int[] rs;
		boolean reuseControlBuffers = false;
		
		int workUnits = prepareReadBuffers(strategy.getStart(),
				strategy.getEnd(), strategy.getTop(), strategy.getBottom(),
				NUM_WORKERS);
		copyRangeBuffers(ctx);
		pbuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getStart().getClass().getSimpleName(), NUM_WORKERS);
		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx, "Integer", NUM_WORKERS);
		abuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getSeed().getClass().getSimpleName(), NUM_WORKERS);
		int global_counter = 0;
		do {
			synchronized(kernel) {
				if (is2D) {
					// TODO
				} else {
					int reuse_steps = reuseControlBuffers ? 1 : 0;
					kernel.setArgs(sbuffer, ebuffer, abuffer, rbuffer, starts.size(), global_counter++, reuse_steps, pbuffer);
				}
				setExtraDataArgs(7, kernel);
				eventsArr[0] = kernel.enqueueNDRange(q, new int[] { NUM_WORKERS }, eventsArr);
			}
			
			reuseControlBuffers = true;
			rs = rbuffer.read(q, eventsArr[0]).getInts();
			for (int i=0; i<workUnits; i++) {
				if (rs[i] == 2) {
					reuseControlBuffers = false;
					break;
				}
			}
			if (reuseControlBuffers) {
				if (System.getenv("DEBUG") != null) {
					R stepXprobe = (R) BufferHelper.extractElementFromBuffer(pbuffer, q, eventsArr[0], strategy.getStart().getClass().getSimpleName());
					System.out.println("Repeating with more granularity: " + stepXprobe + " with control " + rs[0]);
				}
				continue;
			}	
			
			filterAndSplitFirst(workUnits, rs);
			copyRangeBuffers(ctx);
			workUnits = starts.size(); // TODO: Remove this 
		} while (!isDone);
		rbuffer.release();
		PList<T> accs = (PList<T>) BufferHelper.extractFromBuffer(abuffer, q,
				eventsArr[0], strategy.getSeed().getClass()
						.getSimpleName(), NUM_WORKERS);
		
		output = strategy.getSeed();
		for (T acc : accs) {
			output = strategy.combine(output, acc);
		}
		abuffer.release();
		
	}
	
	private void filterAndSplitFirst(int workUnits, int[] rs) {
		int done = 0;
		int partial = 0;
		int zero = 0;
		
		System.out.println("rs: " + rs.length);
		System.out.println("w: " + workUnits);
		
		for (int i=workUnits-1; i>=0; i--) {
			if (rs[i] == 2) {
				done++;
				starts.remove(i);
				ends.remove(i);
			} else if (rs[i] == 1) {
				partial++;
			}else {
				zero++;
			}
		}
		if (done == workUnits) isDone = true;
		
		while (starts.size() < workUnits && starts.size() > 0) {
			int diff = workUnits - starts.size();
			extendFirst(diff);
			System.out.println(starts.size() + "<--");
		}
		
		if (System.getenv("DEBUG") != null) {
			System.out.println("WorkUnits: " + workUnits + ", Done: " + done + ", Partial: " + partial + ", Zero: " + zero);
		}
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
		starts = (PList<R>) CollectionFactory.listFromType(strategy.getStart()
				.getClass().getSimpleName());
		ends = (PList<R>) CollectionFactory.listFromType(strategy.getEnd()
				.getClass().getSimpleName());
		if (is2D) {
			tops = (PList<R2>) CollectionFactory.listFromType(strategy.getTop()
					.getClass().getSimpleName());
			bottoms = (PList<R2>) CollectionFactory.listFromType(strategy
					.getBottom().getClass().getSimpleName());
		}
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
		return RECURSION_LIMIT;
	}

}
