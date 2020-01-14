package aeminium.gpu.backends.gpu.generators;

import aeminium.gpu.backends.gpu.GPUFilter;
import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

import java.util.HashMap;

public class FilterCodeGen extends AbstractCodeGen {
    private String type;
    private String clSource;
    private String[] parameters;

    public FilterCodeGen(GPUFilter filterOp) {
        type = BufferHelper.getCLTypeOf(filterOp.getType());
        clSource = filterOp.getFilterFun().getSource();
        otherSources = filterOp.getOtherSources();
        parameters = filterOp.getFilterFun().getParameters();
        id = filterOp.getFilterFun().getId();
    }

    public FilterCodeGen(String type, String clSource,
                         String[] pars, String id) {
        this(type, clSource, pars, id, "");
    }

    public FilterCodeGen(String type, String clSource,
                         String[] pars, String id, String otherSources) {
        this.type = BufferHelper.getCLTypeOf(type);
        this.clSource = clSource;
        this.otherSources = otherSources;
        this.parameters = pars;
        this.id = id;
    }

    public String getFilterLambdaSource() {
        HashMap<String, String> mapping = new HashMap<>();
        mapping.put("type", type);
        mapping.put("filter_lambda_name", getFilterLambdaName());
        mapping.put("filter_lambda_par", parameters[0]);
        mapping.put("extra_args", getExtraArgs());
        mapping.put("source", clSource);
        Template t = new Template(
                new TemplateWrapper("opencl/FilterLambdaDef.clt"));
        return t.apply(mapping);
    }

    public String getFilterKernelSource() {
        HashMap<String, String> mapping = new HashMap<>();
        mapping.put("type", type);
        mapping.put("filter_lambda_name", getFilterLambdaName());
        mapping.put("filter_kernel_name", getFilterKernelName());

        mapping.put("filter_lambda_def", getFilterLambdaSource());
        mapping.put("otherSources", otherSources);
        mapping.put("extra_args", getExtraArgs());
        mapping.put("extra_args_call", getExtraArgsCall());

        if (isRange) {
            mapping.put("get_input", "filter_global_id");
        } else {
            mapping.put("get_input", "filter_input[filter_global_id]");
        }
        Template t = new Template(new TemplateWrapper("opencl/FilterKernel.clt"));
        return t.apply(mapping);
    }

    public String getFilterLambdaName() {
        return "filter_function_" + id;
    }

    public String getFilterKernelName() {
        return "filter_kernel_" + id;
    }
}
