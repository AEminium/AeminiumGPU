package aeminium.gpu.backends.gpu;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.FilterCodeGen;
import aeminium.gpu.collections.lazyness.Range;
import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaFilter;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GPUFilter<T> extends GPUGenericKernel {

    protected PList<T> input;
    protected PList<T> output;
    protected LambdaFilter filterFun;

    protected CLBuffer<?> inbuffer;
    private CLBuffer<?> outbuffer;

    private FilterCodeGen gen;
    private CLBuffer<?> booleans;
    private CLBuffer<?> prefix_sum;
    private CLBuffer<?> array_length;

    public GPUFilter(PList<T> input, LambdaFilter filterFun, String otherSources) {
        this.input = input;
        this.filterFun = filterFun;
        this.setOtherSources(otherSources);

        gen = new FilterCodeGen(this);
        if (input instanceof Range) {
            gen.setRange(true);
        }
        otherData = OtherData.extractOtherData(filterFun);
        gen.setOtherData(otherData);
    }

    @Override
    public String getKernelName() {
        return gen.getFilterKernelName();
    }

    @Override
    public String getSource() {
        return gen.getFilterKernelSource();
    }

    @Override
    public void prepareBuffers(CLContext ctx) {
        super.prepareBuffers(ctx);
        inbuffer = BufferHelper.createInputBufferFor(ctx, input, end);
        booleans = BufferHelper.createInputOutputBufferFor(ctx, Integer.class.getSimpleName(), end);
        prefix_sum = BufferHelper.createInputOutputBufferFor(ctx, Integer.class.getSimpleName(), end);
        IntList tmp = new IntList();
        tmp.add(size);
        array_length = BufferHelper.createInputOutputBufferFor(ctx, tmp, 1);
        outbuffer = BufferHelper.createOutputBufferFor(ctx, input.getContainingType().getSimpleName(), end);
    }

    @Override
    public void execute(CLContext ctx, CLQueue q) {
        synchronized (kernel) {
            // setArgs will throw an exception at runtime if the types / sizes
            // of the arguments are incorrect
            kernel.setArgs(inbuffer, outbuffer, booleans, prefix_sum, array_length);
            setExtraDataArgs(5, kernel);

            final int MAXTHREADS_PER_BLOCK = 256;
            int threads = this.size / 2 + this.size % 2;
            int blocks = threads / MAXTHREADS_PER_BLOCK + ((threads % MAXTHREADS_PER_BLOCK > 0) ? 1 : 0);
            int threads_per_block = (threads + blocks - 1) / blocks;

            // Ask for 1-dimensional execution of length dataSize, with auto
            // choice of local workgroup size :
            kernelCompletion = kernel.enqueueNDRange(q,
                    new int[]{threads_per_block}, new CLEvent[]{});
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void retrieveResults(CLContext ctx, CLQueue q) {
        output = (PList<T>) BufferHelper.extractFromBuffer(outbuffer, q,
                kernelCompletion, input.getContainingType().getSimpleName(), end);
        this.size = ((PList<Integer>) BufferHelper.extractFromBuffer(array_length, q, kernelCompletion, Integer.class.getSimpleName(), end)).get(0);
        output = output.subList(0, this.size);
    }

    @Override
    public void release() {
        super.release();
        this.inbuffer.release();
        this.outbuffer.release();
        this.booleans.release();
        this.prefix_sum.release();
        this.array_length.release();
    }

    public String getFilterOpenCLSource() {
        return gen.getFilterLambdaSource();
    }

    public String getFilterOpenCLName() {
        return gen.getFilterLambdaName();
    }

    public String getType() {
        return input.getContainingType().getSimpleName();
    }

    public PList<T> getInput() {
        return input;
    }

    public void setInput(PList<T> input) {
        this.input = input;
    }

    public PList<T> getOutput() {
        return output;
    }

    public void setOutput(PList<T> output) {
        this.output = output;
    }

    public LambdaFilter getFilterFun() {
        return filterFun;
    }

    public void setFilterFun(LambdaFilter filterFun) {
        this.filterFun = filterFun;
    }
}
