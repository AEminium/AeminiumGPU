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

public class GPURecursive<R extends Number, R2, T> extends GPUGenericKernel
		implements RecursiveTemplateSource<R, R2, T> {

	public static final int DEFAULT_SPLIT_VALUE = 512;

	public T output;
	public Recursive2DStrategy<R, R2, T> strategy;
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

	private RecursiveCodeGen<R, R2, T> gen;

	public GPURecursive(Recursive2DStrategy<R, R2, T> recursiveStrategy) {
		strategy = recursiveStrategy;
		gen = new RecursiveCodeGen<R, R2, T>(this);
		output = strategy.getSeed();

		otherData = OtherData.extractOtherData(recursiveStrategy);
		gen.setOtherData(otherData);
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		super.prepareBuffers(ctx);
		createEmptyLists();
	}

	public int prepareReadBuffers(R st, R end, R2 top, R2 bottom, int splits,
			int index) {
		Range2D<R, R2> ranges = strategy.split(st, end, top, bottom, splits);
		starts.extendAt(index, ranges.starts);
		ends.extendAt(index, ranges.ends);
		if (ranges.tops != null)
			tops.extendAt(index, ranges.tops);
		if (ranges.bottoms != null)
			bottoms.extendAt(index, ranges.bottoms);
		return starts.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(CLContext ctx, CLQueue q) {

		// initial data;
		int workUnits = prepareReadBuffers(strategy.getStart(),
				strategy.getEnd(), strategy.getTop(), strategy.getBottom(),
				DEFAULT_SPLIT_VALUE, 0);

		rbuffer = (CLBuffer<Integer>) BufferHelper.createOutputBufferFor(ctx,
				"Integer", DEFAULT_SPLIT_VALUE);
		abuffer = BufferHelper.createOutputBufferFor(ctx, strategy.getSeed()
				.getClass().getSimpleName(), DEFAULT_SPLIT_VALUE);
		CLEvent[] eventsArr = new CLEvent[1];

		int processNext = -1;
		while (!isDone) {
			sbuffer = BufferHelper.createInputOutputBufferFor(ctx, starts,
					starts.size());
			ebuffer = BufferHelper.createInputBufferFor(ctx, ends, ends.size());
			if (tops != null)
				tbuffer = BufferHelper.createInputOutputBufferFor(ctx, tops,
						tops.size());
			if (bottoms != null)
				bbuffer = BufferHelper.createInputBufferFor(ctx, bottoms,
						bottoms.size());

			int[] rs;

			PList<T> accs;
			PList<R> startsToRepeat = null;
			PList<R2> topsToRepeat = null;
			
			int use_same_acc = 0;
			while (true) {
				synchronized (kernel) {
					if (strategy.getTop() == null) {
						kernel.setArgs(sbuffer, ebuffer, abuffer, rbuffer, use_same_acc);
					} else {
						kernel.setArgs(sbuffer, ebuffer, tbuffer, bbuffer,
								abuffer, rbuffer, use_same_acc);
					}
					setExtraDataArgs(kernel);

					eventsArr[0] = kernel.enqueueNDRange(q,
							new int[] { workUnits }, eventsArr);
				}
				rs = rbuffer.read(q, eventsArr[0]).getInts();
				accs = (PList<T>) BufferHelper.extractFromBuffer(abuffer, q,
						eventsArr[0], strategy.getSeed().getClass()
								.getSimpleName(), workUnits);

				startsToRepeat = (PList<R>) BufferHelper.extractFromBuffer(
						sbuffer, q, eventsArr[0], strategy.getStart()
								.getClass().getSimpleName(), workUnits);
				if (strategy.getTop() != null)
					topsToRepeat = (PList<R2>) BufferHelper.extractFromBuffer(
							tbuffer, q, eventsArr[0], strategy.getTop()
									.getClass().getSimpleName(), workUnits);

				boolean flag = false;
				for (int i = 0; i < workUnits; i++) {
					if (rs[i] > 0) {
						flag = true;
						break;
					}
				}
				if (flag) {
					use_same_acc = 0;
					break;
				}  else {
					use_same_acc = 1;
				}
			}
			int done = 0;
			for (int i = 0; i < workUnits; i++) {
				if (rs[i] == 2) {
					done++;
				}
				if (rs[i] >= 1) {
					output = strategy.combine(output, accs.get(i));
				}
				if (rs[i] <= 1) {
					if (strategy.getTop() == null) {
						stack.push(new Quad<R, R2>(startsToRepeat.get(i), ends
								.get(i), null, null));
					} else {
						stack.push(new Quad<R, R2>(startsToRepeat.get(i), ends
								.get(i), topsToRepeat.get(i), bottoms.get(i)));
					}
				}
			}
			
			if (System.getenv("DEBUG") != null) {
				System.out.println("Done:" + done + ", q: " + stack.size()
						+ ", next: " + processNext + ", workunits: " + workUnits);
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
					if (processNext < 1)
						processNext = 1;
				}
				int pNext = (processNext > stack.size()) ? stack.size()
						: processNext;
				int steps = DEFAULT_SPLIT_VALUE / pNext;
				for (int k = 0; k < pNext; k++) {
					Quad<R, R2> p = stack.pop();
					int left = DEFAULT_SPLIT_VALUE - workUnits;
					workUnits = prepareReadBuffers(p.s, p.e, p.t, p.b,
							Math.min(steps, left), k * steps);
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
	public void createEmptyLists() {
		starts = (PList<R>) CollectionFactory.listFromType(strategy.getStart()
				.getClass().getSimpleName());
		ends = (PList<R>) CollectionFactory.listFromType(strategy.getEnd()
				.getClass().getSimpleName());
		if (strategy.getTop() != null) {
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

}
