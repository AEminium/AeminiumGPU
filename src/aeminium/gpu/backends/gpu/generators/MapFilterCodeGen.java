package aeminium.gpu.backends.gpu.generators;

import aeminium.gpu.backends.gpu.GPUFilter;
import aeminium.gpu.backends.gpu.GPUMap;
import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

import java.util.HashMap;

//TODO: MapFilter kernel or lazy buffer loading
public class MapFilterCodeGen extends AbstractCodeGen {
    private String inputType;
    private String outputType;
    private String clMapSource;
    private String clFilterSource;
    private String[] map_lambda_parameters;
    private String[] filter_lambda_parameters;

    public MapFilterCodeGen(GPUMap mapOp, GPUFilter filterOp) {
        inputType = BufferHelper.getCLTypeOf(mapOp.getInputType());
        outputType = BufferHelper.getCLTypeOf(mapOp.getOutputType());
        clMapSource = mapOp.getMapFun().getSource();
        clFilterSource = filterOp.getFilterFun().getSource();
        map_lambda_parameters = mapOp.getMapFun().getParameters();
        filter_lambda_parameters = filterOp.getFilterFun().getParameters();
        id = mapOp.getMapFun().getId() + "_" + filterOp.getFilterFun().getId();

        StringBuilder otherSourcesBuilder = new StringBuilder();
        otherSourcesBuilder.append(mapOp.getOtherSources());
        otherSourcesBuilder.append(filterOp.getOtherSources());
        otherSources = otherSourcesBuilder.toString();
    }

    public MapFilterCodeGen(String inputType, String outputType, String clMapSource,
                            String clFilterSource, String[] map_lambda_parameters,
                            String[] filter_lambda_parameters, String id) {
        this(inputType, outputType, clMapSource, clFilterSource, map_lambda_parameters,
                filter_lambda_parameters, id, "");
    }

    public MapFilterCodeGen(String inputType, String outputType, String clMapSource,
                            String clFilterSource, String[] map_lambda_parameters,
                            String[] filter_lambda_parameters, String id, String otherSources) {
        this.inputType = inputType;
        this.outputType = outputType;
        this.clMapSource = clMapSource;
        this.clFilterSource = clFilterSource;
        this.map_lambda_parameters = map_lambda_parameters;
        this.filter_lambda_parameters = filter_lambda_parameters;
        this.id = id;
        this.otherSources = otherSources;
    }

    public String getMapLambdaSource() {
        HashMap<String, String> mapping = new HashMap<String, String>();
        mapping.put("input_type", inputType);
        mapping.put("output_type", outputType);
        mapping.put("map_lambda_name", getMapLambdaName());
        mapping.put("map_lambda_par", map_lambda_parameters[0]);
        mapping.put("extra_args", getExtraArgs());
        mapping.put("source", clMapSource);
        Template t = new Template(
                new TemplateWrapper("opencl/MapLambdaDef.clt"));
        return t.apply(mapping);
    }

    public String getFilterLambdaSource() {
        HashMap<String, String> mapping = new HashMap<>();
        mapping.put("type", outputType);
        mapping.put("filter_lambda_name", getFilterLambdaName());
        mapping.put("filter_lambda_par", filter_lambda_parameters[0]);
        mapping.put("extra_args", getExtraArgs());
        mapping.put("source", clFilterSource);
        Template t = new Template(
                new TemplateWrapper("opencl/FilterLambdaDef.clt"));
        return t.apply(mapping);
    }

    public String getMapFilterKernelSource() {
        HashMap<String, String> mapping = new HashMap<String, String>();

        mapping.put("input_type", inputType);
        mapping.put("output_type", outputType);
        mapping.put("map_lambda_name", getMapLambdaName());
        mapping.put("filter_lambda_name", getFilterLambdaName());
        mapping.put("map_filter_kernel_name", getMapFilterKernelName());

        mapping.put("map_lambda_def", getMapLambdaSource());
        mapping.put("filter_lambda_def", getFilterLambdaSource());
        mapping.put("other_sources", otherSources);

        mapping.put("extra_args", getExtraArgs());
        mapping.put("extra_args_call", getExtraArgsCall());

        if (isRange) {
            mapping.put("get_input", "map_global_id");
        } else {
            mapping.put("get_input", "map_input[map_global_id]");
        }

        Template t = new Template(new TemplateWrapper("opencl/MapFilterKernel.clt"));
        return t.apply(mapping);
    }

    public String getMapLambdaName() {
        return "map_function_" + id;
    }

    public String getFilterLambdaName() {
        return "filter_function_" + id;
    }

    public String getMapFilterKernelName() {
        return "map_filter_kernel_" + id;
    }
}
