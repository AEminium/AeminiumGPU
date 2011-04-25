package aeminium.gpu.benchmarker;

import java.util.ArrayList;
import java.util.HashMap;

import aeminium.gpu.executables.ProgramLogger;

public class LoggerTimer implements ProgramLogger {

	HashMap<String, ArrayList<Long>> map = new HashMap<String, ArrayList<Long>>();
	String prf;
	
	public LoggerTimer(int buffer, int size, String exprname) {
		prf = exprname + "." + size + ".";
	}
		
	@Override
	public void saveTime(String name, long time) {
		if (!map.containsKey(name)) {
			map.put(name, new ArrayList<Long>());
		}
		map.get(name).add(time);
	}

	public void makeAverages() {
		
		for (String name : map.keySet()) {
			long average = 0;
			for (Long t : map.get(name)) {
				average += t;
			}
			average /= map.get(name).size();
			Configuration.set(prf + name, average + "");
		}
	}

}
