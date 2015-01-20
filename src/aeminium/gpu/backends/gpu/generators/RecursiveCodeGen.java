package aeminium.gpu.backends.gpu.generators;

import java.util.HashMap;

import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;


public class RecursiveCodeGen<R extends Number, R2, T> extends AbstractCodeGen {
	private String rType;
	private String r2Type;
	private String tType;
	private String clSource;
	private String splitSource;
	private String[] parameters;
	private int recLimit;
	
	public RecursiveCodeGen(RecursiveTemplateSource<R, R2, T> r) {
		this.rType = r.getRType();
		this.r2Type = r.getR2Type();
		this.tType = r.getTType();
		this.recLimit = r.getRecursionLimit();
		this.splitSource = r.getRecursiveStrategy().getSplitSource();
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
		mapping.put("r2_type", r2Type);
		mapping.put("t_type", tType);
		mapping.put("iter_lambda_name", getIterativeLambdaName());
		mapping.put("iter_lambda_par1", parameters[0]);
		mapping.put("iter_lambda_par2", parameters[1]);
		mapping.put("iter_lambda_par3", parameters[2]);
		mapping.put("iter_lambda_par4", parameters[3]);
		mapping.put("iter_lambda_par5", parameters[4]);
		mapping.put("source", clSource);
		mapping.put("extra_args", getExtraArgs());
		Template t;
		if (r2Type.equals("void*")) {
			t = new Template(new TemplateWrapper("opencl/Recursive1DLambda.clt"));
		} else {
			t = new Template(new TemplateWrapper("opencl/Recursive2DLambda.clt"));
		}
		return t.apply(mapping);
	}
	
	public String getRecursiveKernelSource() {
		HashMap<String, String> mapping = new HashMap<String, String>();
		Template t;

		mapping.put("r_type", rType);
		mapping.put("r2_type", r2Type);
		mapping.put("t_type", tType);
		
		mapping.put("limit", "" + recLimit);

		mapping.put("recursive_kernel_name", getRecursiveKernelName());
		mapping.put("iter_lambda_name", getIterativeLambdaName());
		mapping.put("iter_lambda_def", getRecursiveLambdaSource());
		mapping.put("split", splitSource);
		mapping.put("other_sources", otherSources);
		mapping.put("extra_args", getExtraArgs());
		mapping.put("extra_args_call", getExtraArgsCall());
		if (r2Type.equals("void*")) {
			t = new Template(new TemplateWrapper("opencl/Recursive1DKernel.clt"));
		} else {
			t = new Template(new TemplateWrapper("opencl/Recursive2DKernel.clt"));
		}
		return t.apply(mapping);}
	}
