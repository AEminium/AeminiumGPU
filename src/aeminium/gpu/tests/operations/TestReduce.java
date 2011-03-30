package aeminium.gpu.tests.operations;

import junit.framework.TestCase;
import aeminium.gpu.lists.IntList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.functions.LambdaReducer;

public class TestReduce extends TestCase {
	
	private static int TEST_SIZE = 32;
	
	public void testSumReduce() { 
		PList<Integer> example = new IntList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(1);
		}
		
		Integer output = example.reduce(new LambdaReducer<Integer>() {

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
		assertEquals(10,output.intValue());
	}
	
}
