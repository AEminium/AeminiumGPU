package aeminium.gpu.operations.generator;

import java.io.File;
import java.util.HashMap;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.operations.Map;
import aeminium.gpu.templates.Template;

@SuppressWarnings("rawtypes")
public class MapCodeGen {	
	private Map mapOp;
	private String id;
	
	public MapCodeGen(Map mapOp) {
		this.mapOp = mapOp;
		id = mapOp.getMapFun().getId();
	}
	
	public String getMapLambdaSource() {
		HashMap<String,String> mapping = new HashMap<String,String>();
		mapping.put("input_type", BufferHelper.getCLTypeOf(mapOp.getInputType()));
		mapping.put("output_type", BufferHelper.getCLTypeOf(mapOp.getOutputType()));
		mapping.put("map_lambda_name", getMapLambdaName());
		mapping.put("source", mapOp.getMapFun().getSource());
		Template t = new Template(new File("templates/opencl/MapLambdaDef.clt"));
		return t.apply(mapping);
	}

	public String getMapKernelSource() {
		HashMap<String,String> mapping = new HashMap<String,String>();
		
		mapping.put("input_type", BufferHelper.getCLTypeOf(mapOp.getInputType()));
		mapping.put("output_type", BufferHelper.getCLTypeOf(mapOp.getOutputType()));
		mapping.put("map_lambda_name", getMapLambdaName());
		mapping.put("map_kernel_name", getMapKernelName());
		
		mapping.put("map_lambda_def", getMapLambdaSource());
		mapping.put("other_sources", mapOp.getOtherSources());
		
		Template t = new Template(new File("templates/opencl/MapKernel.clt"));
		return t.apply(mapping);
	}

	public String getMapLambdaName() {
		return "map_function_" + id;
	}
	
	public String getMapKernelName() {
		return "map_kernel_" + id;
	}

}
