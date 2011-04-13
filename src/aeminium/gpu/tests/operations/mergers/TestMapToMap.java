package aeminium.gpu.tests.operations.mergers;

import junit.framework.TestCase;
import aeminium.gpu.lists.IntList;
import aeminium.gpu.lists.PList;
import aeminium.gpu.lists.lazyness.LazyPList;
import aeminium.gpu.operations.functions.LambdaMapper;

public class TestMapToMap extends TestCase {
	
	private static int TEST_SIZE = 10;
	private static int MERGES = 10;
	
	public void testMapIntToInt() { 
		PList<Integer> example = new IntList();
		for (int i = 0; i < TEST_SIZE; i++) {
			example.add(i);
		}
		PList<Integer> output = example;
		int m = 1;
		for (int i = 0; i < MERGES; i++) {
			output = output.map(new LambdaMapper<Integer, Integer>() {

				@Override
				public Integer map(Integer input) {
					return 2 * input;
				}
				
				public String getSource() {
					return "return 2 * input;";
				}
				
			});
			m *= 2;
		}
		
		assertTrue(output instanceof LazyPList<?>);
		assertEquals(MERGES, ((LazyPList<Integer>) output).getLazynessLevel());
		
		for (int i = 0; i < TEST_SIZE; i++) {
			assertEquals(m * i,output.get(i).intValue());
		}
	}
	
}
