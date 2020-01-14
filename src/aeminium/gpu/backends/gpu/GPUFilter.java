package aeminium.gpu.backends.gpu;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.backends.gpu.buffers.OtherData;
import aeminium.gpu.backends.gpu.generators.FilterCodeGen;
import aeminium.gpu.collections.lazyness.Range;
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
        outbuffer = BufferHelper.createOutputBufferFor(ctx, output, end);
    }

    @Override
    public void execute(CLContext ctx, CLQueue q) {
        synchronized (kernel) {
            // setArgs will throw an exception at runtime if the types / sizes
            // of the arguments are incorrect
            kernel.setArgs(inbuffer, outbuffer);
            setExtraDataArgs(2, kernel);

            // Ask for 1-dimensional execution of length dataSize, with auto
            // choice of local workgroup size :
            kernelCompletion = kernel.enqueueNDRange(q,
                    new int[]{end}, new CLEvent[]{});
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void retrieveResults(CLContext ctx, CLQueue q) {
        output = (PList<T>) BufferHelper.extractFromBuffer(outbuffer, q,
                kernelCompletion, end, output);
    }

    @Override
    public void release() {
        super.release();
        this.inbuffer.release();
        this.outbuffer.release();
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
