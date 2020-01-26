package aeminium.gpu.backends.gpu.generators;

import aeminium.gpu.backends.gpu.GPUReduce;
import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

import java.util.HashMap;

public class FilterReduceCodeGen extends AbstractFilterReduceCodeGen {
    private GPUReduce op;
    private String[] filter_parameters;

    public FilterReduceCodeGen(GPUReduce op) {
        this.op = op;
        id = op.getFilterFun().getId() + "_" + op.getReduceFun().getId();
        filter_parameters = op.getFilterFun().getParameters();
    }

    @Override
    public String getFilterLambdaSource() {
        HashMap<String, String> mapping = new HashMap<>();
        mapping.put("type", BufferHelper.getCLTypeOf(op.getInputType()));
        mapping.put("filter_lambda_name", getFilterLambdaName());
        mapping.put("filter_lambda_par", filter_parameters[0]);
        mapping.put("source", op.getFilterFun().getSource());
        mapping.put("extra_args", getExtraArgs());
        Template t = new Template(
                new TemplateWrapper("opencl/FilterLambdaDef.clt"));
        return t.apply(mapping);
    }

    @Override
    public String getReduceLambdaSource() {
        HashMap<String, String> mapping = new HashMap<String, String>();
        mapping.put("input_type", BufferHelper.getCLTypeOf(op.getOutputType()));
        mapping.put("output_type", BufferHelper.getCLTypeOf(op.getOutputType()));
        mapping.put("reduce_lambda_name", getReduceLambdaName());
        mapping.put("reduce_lambda_par1", op.getReduceFun().getParameters()[0]);
        mapping.put("reduce_lambda_par2", op.getReduceFun().getParameters()[1]);
        mapping.put("source", op.getReduceFun().getSource());
        mapping.put("extra_args", getExtraArgs());
        mapping.put("extra_args_call", getExtraArgsCall());
        Template t = new Template(new TemplateWrapper(
                "opencl/ReduceLambdaDef.clt"));
        return t.apply(mapping);
    }

    @Override
    public String getReduceKernelSource() {
        HashMap<String, String> mapping = new HashMap<String, String>();

        mapping.put("input_type", BufferHelper.getCLTypeOf(op.getInputType()));
        mapping.put("output_type", BufferHelper.getCLTypeOf(op.getOutputType()));

        mapping.put("filter_lambda_name", getFilterLambdaName());
        mapping.put("filter_lambda_def", getFilterLambdaSource());

        mapping.put("reduce_lambda_name", getReduceLambdaName());
        mapping.put("reduce_lambda_def", getReduceLambdaSource());

        mapping.put("reduce_kernel_name", getReduceKernelName());
        mapping.put("other_sources", op.getOtherSources());

        mapping.put("extra_args", getExtraArgs());
        mapping.put("extra_args_call", getExtraArgsCall());

        if (isRange) {
            mapping.put("get_input", "inputOffset");
        } else {
            mapping.put("get_input", "filter_input[inputOffset]");
        }

        mapping.put("seed_source", op.getOpenCLSeed());

        Template t = new Template(new TemplateWrapper(
                "opencl/FilterReduceKernel.clt"));
        return t.apply(mapping);
    }

    @Override
    public String getFilterLambdaName() {
        return "filter_function_" + op.getFilterFun().getId();
    }

    @Override
    public String getReduceKernelName() {
        return "reduce_kernel_" + id;
    }

    public String getReduceLambdaName() {
        return "reduce_function_" + op.getReduceFun().getId();
    }
}
