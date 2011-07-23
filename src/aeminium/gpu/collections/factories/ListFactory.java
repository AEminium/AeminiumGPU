package aeminium.gpu.collections.factories;

import aeminium.gpu.collections.lists.BooleanList;
import aeminium.gpu.collections.lists.CharList;
import aeminium.gpu.collections.lists.DoubleList;
import aeminium.gpu.collections.lists.FloatList;
import aeminium.gpu.collections.lists.IntList;
import aeminium.gpu.collections.lists.LongList;
import aeminium.gpu.collections.lists.PList;

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
