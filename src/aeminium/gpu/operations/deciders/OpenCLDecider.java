package aeminium.gpu.operations.deciders;

import java.util.StringTokenizer;

import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.rules.DecisionTable;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


public class OpenCLDecider {

	public static boolean useGPU(int size, String features) {		
		boolean b = decide(size, features);
		if (System.getenv("BENCH") != null) {
			if (b) {
				System.out.println("> GPUchoice");
			} else {
				System.out.println("> CPUchoice");
			}
		}
		return b;
	}

	
	public static boolean decide(int size, String features) {
		if (System.getProperties().containsKey("ForceGPU"))
			return true;
		if (System.getProperties().containsKey("ForceCPU"))
			return false;

		if (size < 5000) { // Small sizes are for CPU
			return false;
		}
		
		if (features == null || features.length() == 0) {
			// Dumb heuristic in case features are absent
			return size > 10000;
		}

		try {
			Classifier classifier = OpenCLDecider.getClassifier();
			Instance i = new DenseInstance(30);
			StringTokenizer split = new StringTokenizer(features);
			int c = 0;
			while (split.hasMoreElements()) {
				i.setValue(c, Integer.parseInt(split.nextToken()));
				c++;
			}
			return classifier.classifyInstance(i) == 1;
		} catch (Exception e) {
			return false;
		} 
	}
	
	private static Classifier classifier = null;
	private static Classifier getClassifier() throws Exception {
		if (OpenCLDecider.classifier != null) return OpenCLDecider.classifier; 
		// Alcides Fonseca and Bruno Cabral,AeminiumGPU: An Intelligent Framework for GPU Programming, in Facing the Multicore-Challenge III, 2012
	    Instances randData = DataSource.read("dataset/features_processed.arff");
	    randData.setClassIndex(randData.numAttributes() - 1);
	    
	    CostSensitiveClassifier c = new CostSensitiveClassifier();
	    c.setMinimizeExpectedCost(true);
	    c.setClassifier(new DecisionTable());
	    
	    CostMatrix cm = new CostMatrix(2);
	    cm.initialize();
	    cm.setElement(0, 1, 0.4);
	    cm.setElement(1, 0, 0.6);
	    c.setCostMatrix(cm);
	    OpenCLDecider.classifier = c;
		return c;
	}
	
}
