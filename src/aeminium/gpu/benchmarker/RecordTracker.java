package aeminium.gpu.benchmarker;

import java.util.ArrayList;
import java.util.HashMap;

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
			long average = 0;
			for (Long t : store.get(name)) {
				average += t;
			}
			average /= store.get(name).size();
			Configuration.set(name, average + "");
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
