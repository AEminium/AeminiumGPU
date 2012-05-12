package aeminium.gpu.recorder;

import aeminium.gpu.executables.ProgramLogger;

public class LoggerTimer implements ProgramLogger {
	RecordTracker tracker;
	int size;
	String exprname;
	String pref;

	public LoggerTimer(String pref, int bufferSize, int arraySize,
			String exprname, RecordTracker tracker) {
		this.tracker = tracker;
		this.size = arraySize;
		this.exprname = exprname;
		this.pref = pref + "."; /* CPU or GPU */
	}

	@Override
	public void saveTime(String name, long time) {
		if (name.contains("buffer")) {
			tracker.store(pref + name + "." + size, time);
		} else {
			tracker.store(pref + name + "." + size + "." + exprname, time);
		}
	}
}
