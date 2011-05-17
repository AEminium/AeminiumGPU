package aeminium.gpu.recorder;

import aeminium.gpu.executables.ProgramLogger;

public class LoggerTimer implements ProgramLogger {
	RecordTracker tracker;
	String prf;
	String size_prf;
	
	public LoggerTimer(int buffer, int size, String exprname, RecordTracker tracker) {
		size_prf = size + ".";
		prf = exprname + "." + size_prf;
		this.tracker = tracker;
	}
		
	@Override
	public void saveTime(String name, long time) {
		if (name.contains("buffer")) {
			tracker.store(size_prf + name, time);
		} else {
			tracker.store(prf + name, time);
		}
	}
}
