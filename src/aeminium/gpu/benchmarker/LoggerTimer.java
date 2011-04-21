package aeminium.gpu.benchmarker;

import aeminium.gpu.executables.ProgramLogger;

public class LoggerTimer implements ProgramLogger {

	String prf;
	
	public LoggerTimer(int size, String exprname) {
		prf = exprname + "." + size + ".";
	}
		
	@Override
	public void saveTime(String name, long time) {
		Configuration.append(prf + name, time + "");
	}

}
