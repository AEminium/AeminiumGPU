package aeminium.gpu.recorder;

import aeminium.gpu.executables.ProgramLogger;

public class LoggerTimer implements ProgramLogger {
	RecordTracker tracker;
	int size;
	String exprname;
	
	public LoggerTimer(int bufferSize, int arraySize, String exprname, RecordTracker tracker) {
		this.tracker = tracker;
		this.size = arraySize;
		this.exprname = exprname;
	}
		
	@Override
	public void saveTime(String name, long time) {
		if (name.contains("buffer")) {
			tracker.store("gpu." + name + "." + size, time);
		} else {
			tracker.store("gpu." + name + "." + size + "." + exprname, time);
		}
	}
}
