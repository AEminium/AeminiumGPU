package aeminium.gpu.backends.gpu.generators;

import java.util.HashMap;

import aeminium.gpu.backends.gpu.GPURecursiveCall;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

@SuppressWarnings("rawtypes")
public class RecursiveCallCodeGen extends AbstractCodeGen {
	private String rType;
	private String aType;
	private String clSource;
	private String[] parameters;
	private String spawnSource;
	private int maxSpawn;
	
	public RecursiveCallCodeGen(GPURecursiveCall r) {
		this.rType = r.getRType();
		this.aType = r.getAType();
		this.clSource = r.getRecursiveStrategy().getSource();
		this.parameters = r.getRecursiveStrategy().getParameters();
		this.id = r.getRecursiveStrategy().getId();
		this.spawnSource = r.getRecursiveStrategy().getSplitSource();
		this.maxSpawn = r.getStackSize();
	}
	
	public String getIterativeLambdaName() {
		return "rec_iterative_" + id;
	}

	public String getRecursiveKernelName() {
		return "rec_kernel_" + id;
	}
	
	public String getRecursiveLambdaSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		
		System.out.println(rType);
		
		mapping.put("r_type", rType);
		mapping.put("a_type", aType);
		mapping.put("iter_lambda_name", getIterativeLambdaName());
		mapping.put("iter_lambda_par1", parameters[0]);
		mapping.put("iter_lambda_par2", parameters[1]);
		mapping.put("source", clSource);
		mapping.put("extra_args", getExtraArgs());
		Template t = new Template(new TemplateWrapper(
				"opencl/RecursiveCallIterativeLambda.clt"));
		return t.apply(mapping);
	}
	
	public String getRecursiveKernelSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		Template t;

		mapping.put("r_type", rType);
		mapping.put("a_type", aType);
		mapping.put("spawn_arg", parameters[0]);
		mapping.put("spawn", spawnSource);
		mapping.put("max_spawn", "" + maxSpawn);

		mapping.put("recursive_kernel_name", getRecursiveKernelName());
		mapping.put("iter_lambda_name", getIterativeLambdaName());
		mapping.put("iter_lambda_def", getRecursiveLambdaSource());
		mapping.put("other_sources", otherSources);
		mapping.put("extra_args", getExtraArgs());
		mapping.put("extra_args_call", getExtraArgsCall());
		t = new Template(new TemplateWrapper("opencl/RecursiveCallKernel.clt"));
		return t.apply(mapping);
	}
}

