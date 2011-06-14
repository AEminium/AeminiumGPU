package aeminium.gpu.recorder;

import java.util.ArrayList;
import java.util.HashMap;

import aeminium.gpu.utils.Statistics;

public class RecordTracker {

	HashMap<String, ArrayList<Long>> store = new HashMap<String, ArrayList<Long>>();
	
	public void store(String key, long val) {
		if (!store.containsKey(key)) {
			store.put(key, new ArrayList<Long>());
		}
		store.get(key).add(val);
	}
	
	public void makeAverages() {
		for (String name : store.keySet()) {
			long[] data = new long[store.get(name).size()];
			int i = 0;
			if (System.getenv("MEA") != null) {
				System.out.print(name + " --> ");
			}
			long average = 0;
			for (Long t : store.get(name)) {
				if (System.getenv("MEA") != null) {
					System.out.print(t + ",");
				}
				average += t;
				data[i++] = t;
			}
			average /= store.get(name).size();
			Configuration.set(name, average + "");
			
			long stddev = (long) Statistics.standard_deviation(data);
			Configuration.set(name + ".stddev", stddev + "");
		}
	}
	
	public void makeMaximum() {
		for (String name : store.keySet()) {
			long max = 0;
			for (Long t : store.get(name)) {
				if (t > max) max = t; 
			}
			Configuration.set(name, max + "");
		}
	}
}
