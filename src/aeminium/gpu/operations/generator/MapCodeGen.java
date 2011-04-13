package aeminium.gpu.operations.generator;

import java.util.HashMap;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.operations.Map;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

@SuppressWarnings("rawtypes")
public class MapCodeGen {	
	
	private String inputType;
	private String outputType;
	private String clSource;
	private String otherSources = "";
	private String id;
	private String[] parameters;
	
	public MapCodeGen(Map mapOp) {
		inputType = BufferHelper.getCLTypeOf(mapOp.getInputType());
		outputType = BufferHelper.getCLTypeOf(mapOp.getOutputType());
		clSource = mapOp.getMapFun().getSource();
		otherSources = mapOp.getOtherSources();
		parameters = mapOp.getMapFun().getParameters();
		id = mapOp.getMapFun().getId();
	}
	
	public MapCodeGen(String inputType, String outputType, 
			String clSource, String[] pars, String id) {
		this(inputType, outputType, clSource, pars, id, "");
	}
	
	public MapCodeGen(String inputType, String outputType, 
			String clSource, String[] pars, String id, String otherSources) {
		this.inputType = BufferHelper.getCLTypeOf(inputType);
		this.outputType = BufferHelper.getCLTypeOf(outputType);
		this.clSource =  clSource;
		this.otherSources = otherSources;
		this.parameters = pars;
		this.id = id;
	}
	
	public String getMapLambdaSource() {
		HashMap<String,String> mapping = new HashMap<String,String>();
		mapping.put("input_type", inputType);
		mapping.put("output_type", outputType);
		mapping.put("map_lambda_name", getMapLambdaName());
		mapping.put("map_lambda_par", parameters[0]);
		mapping.put("source", clSource);
		Template t = new Template(new TemplateWrapper("opencl/MapLambdaDef.clt"));
		return t.apply(mapping);
	}

	public String getMapKernelSource() {
		HashMap<String,String> mapping = new HashMap<String,String>();
		
		mapping.put("input_type", inputType);
		mapping.put("output_type", outputType);
		mapping.put("map_lambda_name", getMapLambdaName());
		mapping.put("map_kernel_name", getMapKernelName());
		
		mapping.put("map_lambda_def", getMapLambdaSource());
		mapping.put("other_sources", otherSources);
		
		Template t = new Template(new TemplateWrapper("opencl/MapKernel.clt"));
		return t.apply(mapping);
	}

	public String getMapLambdaName() {
		return "map_function_" + id;
	}
	
	public String getMapKernelName() {
		return "map_kernel_" + id;
	}

}
