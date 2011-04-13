package aeminium.gpu.lists.factories;

import aeminium.gpu.lists.BooleanList;
import aeminium.gpu.lists.CharList;
import aeminium.gpu.lists.DoubleList;
import aeminium.gpu.lists.FloatList;
import aeminium.gpu.lists.IntList;
import aeminium.gpu.lists.LongList;
import aeminium.gpu.lists.PList;

public class ListFactory {
	public static PList<?> fromType(String outputType) {
		if (outputType.equals("Integer")) {
			return new IntList();
		}
		if (outputType.equals("Float")) {
			return new FloatList();
		}
		if (outputType.equals("Double")) {
			return new DoubleList();
		}
		if (outputType.equals("Long")) {
			return new LongList();
		}
		if (outputType.equals("Character")) {
			return new CharList();
		}
		if (outputType.equals("Boolean")) {
			return new BooleanList();
		}
		return null;
	}
}
