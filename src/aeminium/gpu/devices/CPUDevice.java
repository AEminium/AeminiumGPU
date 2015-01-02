package aeminium.gpu.devices;

import java.util.Arrays;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class CPUDevice {
	public static int rtcalls = 0;
	public final static Runtime rt = Factory.getRuntime();
	
	public static void init() {
		if (rtcalls++ == 0)
			rt.init();
	}

	public static void shutdown() {
		rtcalls--;
		if (rtcalls < 1) {
			rt.shutdown();
			rtcalls = 0;
		}
	}	
	
	public static Task submit(Body b) {
		init();
		Task t = rt.createNonBlockingTask(b, Hints.LOOPS);
		CPUDevice.rt.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
		return t;
	}
	
	public static Task submit(Body b, Task... dep) {
		if (rtcalls == 0) init();
		Task t = rt.createNonBlockingTask(b, Hints.LOOPS);
		CPUDevice.rt.schedule(t, Runtime.NO_PARENT, Arrays.asList(dep));
		return t;
	}
	
	public static Task submit(Task t) {
		if (rtcalls == 0) init();
		CPUDevice.rt.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
		return t;
	}
	
	public static Task submit(Task t, Task... dep) {
		if (rtcalls == 0) init();
		CPUDevice.rt.schedule(t, Runtime.NO_PARENT, Arrays.asList(dep));
		return t;
	}
	
	public static int getParallelism() {
		return java.lang.Runtime.getRuntime().availableProcessors();
	}
	
	public static void waitFor(Task t) {
		rt.waitToEmpty();
		shutdown();
	}
}
