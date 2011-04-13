package aeminium.gpu.operations.generator;

import java.io.File;
import java.util.HashMap;

import aeminium.gpu.buffers.BufferHelper;
import aeminium.gpu.operations.Reduce;
import aeminium.gpu.templates.Template;

@SuppressWarnings("rawtypes")
public class ReduceCodeGen {
	private Reduce reduceOp;
	private String id;

	public ReduceCodeGen(Reduce reduceOp) {
		this.reduceOp = reduceOp;
		id = reduceOp.getReduceFun().getId();
	}

	public String getReduceLambdaSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		mapping.put("input_type",
				BufferHelper.getCLTypeOf(reduceOp.getInputType()));
		mapping.put("output_type",
				BufferHelper.getCLTypeOf(reduceOp.getOutputType()));
		mapping.put("reduce_lambda_name", getReduceLambdaName());
		mapping.put("source", reduceOp.getReduceFun().getSource());
		mapping.put("reduce_lambda_par1", reduceOp.getReduceFun().getParameters()[0]);
		mapping.put("reduce_lambda_par2", reduceOp.getReduceFun().getParameters()[1]);
		Template t = new Template(new File(
				"templates/opencl/ReduceLambdaDef.clt"));
		return t.apply(mapping);
	}

	public String getReduceKernelSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();

		mapping.put("input_type",
				BufferHelper.getCLTypeOf(reduceOp.getInputType()));
		mapping.put("output_type",
				BufferHelper.getCLTypeOf(reduceOp.getOutputType()));
		mapping.put("reduce_lambda_name", getReduceLambdaName());
		mapping.put("reduce_kernel_name", getReduceKernelName());

		mapping.put("reduce_lambda_def", getReduceLambdaSource());
		mapping.put("other_sources", reduceOp.getOtherSources());

		mapping.put("seed_source", reduceOp.getOpenCLSeed());

		Template t = new Template(new File("templates/opencl/ReduceKernel.clt"));
		return t.apply(mapping);
	}

	public String getReduceLambdaName() {
		return "reduce_function_" + id;
	}

	public String getReduceKernelName() {
		return "reduce_kernel_" + id;
	}

}
