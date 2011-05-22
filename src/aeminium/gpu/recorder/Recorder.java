package aeminium.gpu.recorder;

import aeminium.gpu.devices.DefaultDeviceFactory;
import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.lists.FloatList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.Map;
import aeminium.gpu.operations.functions.LambdaMapper;

public class Recorder {
	
	int times = 30;
	public int[] sizes = new int[] {
			10, 100, 1000, 10000, 100000, 1000000, 10000000
	};
	
	GPUDevice dev = new DefaultDeviceFactory().getDevice();
	RecordTracker tracker = new RecordTracker();
	
	public static void main(String[] args) {
		Recorder b = new Recorder();
		b.run();
	}
	
	public void run() {
		executeExprMultipleSizes("unit", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return input;
			}
			public String getSource() {
				return "return input;";
			}
		});
		executeExprMultipleSizes("plus", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return input-input;
			}
			public String getSource() {
				return "return input+input;";
			}
		});
		executeExprMultipleSizes("minus", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return input+input;
			}
			public String getSource() {
				return "return input-input;";
			}
		});
		executeExprMultipleSizes("mul", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return input*input;
			}
			public String getSource() {
				return "return input*input;";
			}
		});
		executeExprMultipleSizes("eq", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return (input==1f) ? 1f : 2f;
			}
			public String getSource() {
				return "return (input==1.0) ? 1.0 : 2.0;";
			}
		});
		executeExprMultipleSizes("sin", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return (float) Math.sin(input);
			}
			public String getSource() {
				return "return sin(input);";
			}
		});
		executeExprMultipleSizes("cos", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return (float) Math.cos(input);
			}
			public String getSource() {
				return "return cos(input);";
			}
		});
		executeExprMultipleSizes("pow", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return (float) Math.pow(input,4);
			}
			public String getSource() {
				return "return pow(input,4);";
			}
		});
		executeExprMultipleSizes("log", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return (float) Math.log(input);
			}
			public String getSource() {
				return "return log(input);";
			}
		});
		executeExprMultipleSizes("floor", new LambdaMapper<Float,Float>() {
			@Override
			public Float map(Float input) {
				return (float) Math.floor(input);
			}
			public String getSource() {
				return "return floor(input);";
			}
		});
		finish();
	}
	
	private void finish() {
		tracker.makeAverages();
	}

	public void executeExprMultipleSizes(String name, LambdaMapper<Float,Float> expr) {
		PList<Float> input;
		for (int size : sizes) {
			input = new FloatList();
			for (int i = 0; i < size; i++) {
				input.add(new Float(i));
			}
			executeExprMultipleTimes(name, expr, input);
		}
	}
	
	
	public void executeExprMultipleTimes(String name, LambdaMapper<Float,Float> expr, PList<Float> input) {
		System.out.println("Op:" + name + ", Size:" + input.size());
		LoggerTimer loggerGPU = new LoggerTimer("gpu",times, input.size(), name, tracker);
		LoggerTimer loggerCPU = new LoggerTimer("cpu",times, input.size(), name, tracker);
		for (int i = 0; i < times; i++) {
			executeExpr(name, expr, input, loggerGPU, loggerCPU);
			System.gc();
		}
	}
	
	public void executeExpr(String name, LambdaMapper<Float,Float> expr, PList<Float> input, LoggerTimer loggerGPU, LoggerTimer loggerCPU) {
		// Record GPU Times
		Map<Float, Float> op = new Map<Float, Float>(expr, input, dev);
		op.setLogger(loggerGPU);
		dev.execute(op);
		
		// Record CPU Times
		op = new Map<Float, Float>(expr, input, dev);
		long t = System.nanoTime();
		op.cpuExecution();
		loggerCPU.saveTime("execution", System.nanoTime() - t);	
	}
}
