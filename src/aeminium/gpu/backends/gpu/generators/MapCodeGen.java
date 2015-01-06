package aeminium.gpu.backends.gpu.generators;

import java.util.HashMap;

import aeminium.gpu.backends.gpu.GPUMap;
import aeminium.gpu.backends.gpu.buffers.BufferHelper;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

@SuppressWarnings("rawtypes")
public class MapCodeGen extends AbstractCodeGen {

	private String inputType;
	private String outputType;
	private String clSource;
	private String[] parameters;

	public MapCodeGen(GPUMap mapOp) {
		inputType = BufferHelper.getCLTypeOf(mapOp.getInputType());
		outputType = BufferHelper.getCLTypeOf(mapOp.getOutputType());
		clSource = mapOp.getMapFun().getSource();
		otherSources = mapOp.getOtherSources();
		parameters = mapOp.getMapFun().getParameters();
		id = mapOp.getMapFun().getId();
	}

	public MapCodeGen(String inputType, String outputType, String clSource,
			String[] pars, String id) {
		this(inputType, outputType, clSource, pars, id, "");
	}

	public MapCodeGen(String inputType, String outputType, String clSource,
			String[] pars, String id, String otherSources) {
		this.inputType = BufferHelper.getCLTypeOf(inputType);
		this.outputType = BufferHelper.getCLTypeOf(outputType);
		this.clSource = clSource;
		this.otherSources = otherSources;
		this.parameters = pars;
		this.id = id;
	}

	public String getMapLambdaSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		mapping.put("input_type", inputType);
		mapping.put("output_type", outputType);
		mapping.put("map_lambda_name", getMapLambdaName());
		mapping.put("map_lambda_par", parameters[0]);
		mapping.put("extra_args", getExtraArgs());
		mapping.put("source", clSource);
		Template t = new Template(
				new TemplateWrapper("opencl/MapLambdaDef.clt"));
		return t.apply(mapping);
	}

	public String getMapKernelSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();

		mapping.put("input_type", inputType);
		mapping.put("output_type", outputType);
		mapping.put("map_lambda_name", getMapLambdaName());
		mapping.put("map_kernel_name", getMapKernelName());

		mapping.put("map_lambda_def", getMapLambdaSource());
		mapping.put("other_sources", otherSources);
		mapping.put("map_extra_args", getExtraArgs());
		mapping.put("map_extra_args_call", getExtraArgsCall());

		if (isRange) {
			mapping.put("get_input", "map_global_id");
		} else {
			mapping.put("get_input", "map_input[map_global_id]");
		}

		Template t = new Template(new TemplateWrapper("opencl/MapKernel.clt"));
		return t.apply(mapping);
	}

	public String getMapLambdaName() {
		return "map_function_" + id;
	}

	public String getMapKernelName() {
		return "map_kernel_" + id;
	}

	public boolean isRange() {
		return isRange;
	}

	public void setRange(boolean isRange) {
		this.isRange = isRange;
	}

}
