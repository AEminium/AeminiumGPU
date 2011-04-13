package aeminium.gpu.utils;

import aeminium.gpu.lists.PList;

public class ListPrinter {
	public static <O> void printInline(PList<O> list) {
		for (int i = 0; i < list.size(); i++) {
			System.out.print(list.get(i) + ", ");
		}
		System.out.println();
	}
}
