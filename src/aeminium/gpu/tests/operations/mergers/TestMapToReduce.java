package aeminium.gpu.tests.operations.mergers;

import junit.framework.TestCase;
import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;
import aeminium.gpu.operations.functions.LambdaReducer;

public class TestMapToReduce extends TestCase {
	
	private static int TEST_SIZE = 32;
	
	public void testMapToReduce() { 
		PList<Integer> example = new IntList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(1);
		}
		
		Integer output = example.map(new LambdaMapper<Integer,Integer>() {

			@Override
			public Integer map(Integer input) {
				return 2 * input;
			}
			
			@Override
			public String getSource() {
				return "return 2 * input;";
			}
			
		}).reduce(new LambdaReducer<Integer>() {

			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other;
			}

			@Override
			public Integer getSeed() {
				return 0;
			}
			
			@Override
			public String getSource() {
				return "return reduce_input_first + reduce_input_second;";
			}
			
		});
		assertEquals(2 * TEST_SIZE,output.intValue());
	}
	
	public void testMapToMapToReduce() { 
		PList<Integer> example = new IntList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(1);
		}
		
		Integer output = example.map(new LambdaMapper<Integer,Integer>() {

			@Override
			public Integer map(Integer input) {
				return 2 * input;
			}
			
			@Override
			public String getSource() {
				return "return 2 * input;";
			}
			
		}).map(new LambdaMapper<Integer,Integer>() {

			@Override
			public Integer map(Integer input) {
				return 3 * input;
			}
			
			@Override
			public String getSource() {
				return "return 3 * input;";
			}
			
		}).reduce(new LambdaReducer<Integer>() {

			@Override
			public Integer combine(Integer input, Integer other) {
				return input + other;
			}

			@Override
			public Integer getSeed() {
				return 0;
			}
			
			@Override
			public String getSource() {
				return "return reduce_input_first + reduce_input_second;";
			}
			
		});
		assertEquals(2 * 3 * TEST_SIZE,output.intValue());
	}
	
	public void testMapToReduceWithDifferentTypes() { 
		PList<Integer> example = new IntList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(1);
		}
		
		Float output = example.map(new LambdaMapper<Integer,Float>() {

			@Override
			public Float map(Integer input) {
				return 2.0f * input;
			}
			
			@Override
			public String getSource() {
				return "return 2.0 * input;";
			}
			
		}).reduce(new LambdaReducer<Float>() {

			@Override
			public Float combine(Float input, Float other) {
				return input + other;
			}

			@Override
			public Float getSeed() {
				return 0f;
			}
			
			@Override
			public String getSource() {
				return "return reduce_input_first + reduce_input_second;";
			}
			
		});
		assertEquals((float) (2.0 * TEST_SIZE),output.floatValue());
	}
	
}
