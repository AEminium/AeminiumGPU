package aeminium.gpu.tests.operations;

import junit.framework.TestCase;
import aeminium.gpu.lists.IntList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class TestMap extends TestCase {
	
	
	public void testMap() { 
		PList<Integer> example = new IntList();
		for (int i = 0; i < 10000; i++) {
			example.add(i);
		}
		PList<Integer> output = example.map(new LambdaMapper<Integer, Integer>() {

			@Override
			public Integer map(Integer input) {
				return 2 * input;
			}
			
			public String getSource() {
				return "return 2 * input;";
			}
			
		});
		for (int i = 0; i < 10000; i++) {
			assertEquals(2 * i,output.get(i).intValue());
		}
		
	}
	
}
