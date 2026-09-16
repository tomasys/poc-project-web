package com.tomatosystem.core.profiler;
import java.io.IOException;

import com.tomatosystem.core.profiler.timer.ProfilingTimerStack;

public class UtilProfilingStack {
	
	public static void push(String name) {
		try {
			ProfilingTimerStack.push(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void pop(String name) {
		try {
			ProfilingTimerStack.pop(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String getPop(String name) {
		try {
			return ProfilingTimerStack.getPop(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
