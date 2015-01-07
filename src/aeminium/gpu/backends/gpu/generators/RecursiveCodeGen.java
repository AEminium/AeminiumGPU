package aeminium.gpu.backends.gpu.generators;

import java.util.HashMap;

import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;

@SuppressWarnings("rawtypes")
public class RecursiveCodeGen extends AbstractCodeGen {
	private String rType;
	private String tType;
	private String clSource;
	private String[] parameters;
	
	
	public RecursiveCodeGen(RecursiveTemplateSource r) {
		this.rType = r.getRType();
		this.tType = r.getTType();
		this.clSource = r.getRecursiveStrategy().getSource();
		this.parameters = r.getRecursiveStrategy().getParameters();
		this.id = r.getRecursiveStrategy().getId();
	}
	
	public String getIterativeLambdaName() {
		return "rec_iterative_" + id;
	}

	public String getRecursiveKernelName() {
		return "rec_kernel_" + id;
	}
	
	public String getRecursiveLambdaSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		mapping.put("r_type", rType);
		mapping.put("t_type", tType);
		mapping.put("iter_lambda_name", getIterativeLambdaName());
		mapping.put("iter_lambda_par1", parameters[0]);
		mapping.put("iter_lambda_par2", parameters[1]);
		mapping.put("iter_lambda_par3", parameters[2]);
		mapping.put("source", clSource);
		mapping.put("extra_args", getExtraArgs());
		Template t = new Template(new TemplateWrapper(
				"opencl/RecursiveIterativeLambda.clt"));
		return t.apply(mapping);
	}
	
	public String getRecursiveKernelSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		Template t;

		mapping.put("r_type", rType);
		mapping.put("t_type", tType);

		mapping.put("recursive_kernel_name", getRecursiveKernelName());
		mapping.put("iter_lambda_name", getIterativeLambdaName());
		mapping.put("iter_lambda_def", getRecursiveLambdaSource());
		mapping.put("other_sources", otherSources);
		mapping.put("extra_args", getExtraArgs());
		mapping.put("extra_args_call", getExtraArgsCall());
		t = new Template(new TemplateWrapper("opencl/RecursiveKernel.clt"));
		return t.apply(mapping);}
	}
