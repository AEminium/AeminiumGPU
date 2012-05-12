package aeminium.gpu.operations.utils;

public class UniqCounter {

	static private int c = 0;

	public static String getNewId() {
		return "" + c++;
	}

}
