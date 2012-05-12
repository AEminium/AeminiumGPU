package aeminium.gpu.operations.generator;

import java.util.HashMap;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

@SuppressWarnings("rawtypes")
public class ReduceCodeGen {

	private String inputType;
	private String outputType;
	private String clSource;
	private String seedSource;
	private String otherSources = "";
	private String id;
	private String[] parameters;
	private boolean isRange = false;
	private boolean hasSeed = false;

	public ReduceCodeGen(String inputType, String outputType, String clSource,
			String seedSource, String[] pars, String id) {
		this(inputType, outputType, clSource, seedSource, pars, id, "");
	}

	public ReduceCodeGen(String inputType, String outputType, String clSource,
			String seedSource, String[] pars, String id, String otherSources) {
		this.inputType = BufferHelper.getCLTypeOf(inputType);
		this.outputType = BufferHelper.getCLTypeOf(outputType);
		this.clSource = clSource;
		this.seedSource = seedSource;
		this.otherSources = otherSources;
		this.parameters = pars;
		this.id = id;
	}

	public ReduceCodeGen(ReduceTemplateSource reduceOp) {
		inputType = BufferHelper.getCLTypeOf(reduceOp.getInputType());
		outputType = BufferHelper.getCLTypeOf(reduceOp.getOutputType());
		clSource = reduceOp.getReduceFun().getSource();
		seedSource = reduceOp.getOpenCLSeed();
		otherSources = reduceOp.getOtherSources();
		parameters = reduceOp.getReduceFun().getParameters();
		id = reduceOp.getReduceFun().getId();
	}

	public String getReduceLambdaSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		mapping.put("input_type", inputType);
		mapping.put("output_type", outputType);
		mapping.put("reduce_lambda_name", getReduceLambdaName());
		mapping.put("reduce_lambda_par1", parameters[0]);
		mapping.put("reduce_lambda_par2", parameters[1]);
		mapping.put("source", clSource);
		Template t = new Template(new TemplateWrapper(
				"opencl/ReduceLambdaDef.clt"));
		return t.apply(mapping);
	}

	public String getReduceKernelSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		Template t;

		mapping.put("input_type", inputType);
		mapping.put("output_type", outputType);

		mapping.put("reduce_lambda_name", getReduceLambdaName());
		mapping.put("reduce_kernel_name", getReduceKernelName());
		mapping.put("reduce_lambda_def", getReduceLambdaSource());
		mapping.put("other_sources", otherSources);

		if (isRange) {
			mapping.put("get_input", "reduce_input[inputOffset]");
		} else {
			mapping.put("get_input", "inputOffset");
		}

		if (hasSeed) {
			mapping.put("seed_source", seedSource);
			t = new Template(new TemplateWrapper(
					"opencl/ReduceWithSeedKernel.clt"));
		} else {
			t = new Template(new TemplateWrapper("opencl/ReduceKernel.clt"));
		}

		return t.apply(mapping);
	}

	public String getReduceLambdaName() {
		return "reduce_function_" + id;
	}

	public String getReduceKernelName() {
		return "reduce_kernel_" + id;
	}

	public boolean isRange() {
		return isRange;
	}

	public void setRange(boolean isRange) {
		this.isRange = isRange;
	}

	public void setHasSeed(boolean b) {
		hasSeed = b;
	}

}
