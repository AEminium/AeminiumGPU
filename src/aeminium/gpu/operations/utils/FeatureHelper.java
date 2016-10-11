package aeminium.gpu.operations.utils;

import aeminium.gpu.backends.gpu.buffers.BufferHelper;

public class FeatureHelper {

	public static String getFullFeatures(String base, int is, String it, int os, String ot, int op) {
		if (base == null) return null;
		String toSize = BufferHelper.getSize(is, it).toString();
		String fromSize = BufferHelper.getSize(os, ot).toString();
		return base + "," + toSize + "," + fromSize + "," + op;
	}
	
	
	public static String sumFeatures(String f1, String f2) {
		if (f1 == null) return f2;
		if (f2 == null) return f1;
		String[] parts;
		int[] features = new int[27];
		parts = f1.split(",");
		int i = 0;
		for (String p : parts) {
			int t = Integer.parseInt(p);
			features[i++] = t;
		}
		parts = f2.split(",");
		i = 0;
		for (String p : parts) {
			int t = Integer.parseInt(p);
			features[i++] += t;
		}
		StringBuilder sb = new StringBuilder();
		for (i=0; i<features.length; i++) {
			sb.append(features[i] + ",");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
}
