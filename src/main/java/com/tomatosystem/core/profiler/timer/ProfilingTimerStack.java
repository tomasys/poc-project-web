package com.tomatosystem.core.profiler.timer;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tomatosystem.core.resource.AppProperties;

public class ProfilingTimerStack {
	
	public static ThreadLocal current = new ThreadLocal();
	private final static Logger logger = LogManager.getLogger(ProfilingTimerStack.class);
	
	public static void push(String name) throws IOException {
		if (!isActive()) return;
		
		ProfilingTimerBean newTimer = new ProfilingTimerBean(name);
		newTimer.setStartTime();
		
		ProfilingTimerBean currentTimer = (ProfilingTimerBean) current.get();
		if (currentTimer != null) {
			currentTimer.addChild(newTimer);
		}
		current.set(newTimer);
	}
	
	public static void pop(String name) throws IOException {
		if (!isActive()) return;
		ProfilingTimerBean currentTimer = (ProfilingTimerBean) current.get();
		
		if (currentTimer != null && name != null && name.equals(currentTimer.getResource())) {
			currentTimer.setEndTime();
			ProfilingTimerBean parent = currentTimer.getParent();
			if (parent == null) {
				print(currentTimer);current.set(null);
			} else {
				current.set(parent);
			}
		} else {
			if (currentTimer != null) {
				print(currentTimer);current.set(null);
			}
		}
	}
	
	public static String getPop(String name) throws IOException {
		if (!isActive()) return "";
		ProfilingTimerBean currentTimer = (ProfilingTimerBean) current.get();
		
		String time = "";
		if (currentTimer != null && name != null && name.equals(currentTimer.getResource())) {
			currentTimer.setEndTime();
			ProfilingTimerBean parent = currentTimer.getParent();
			if (parent == null) {
				time = get(currentTimer);
				print(currentTimer);
				current.set(null);
			} else {
				current.set(parent);
			}
		} else {
			if (currentTimer != null) {
				time = get(currentTimer);
				print(currentTimer);
				current.set(null);
			}
		}
		return time;
	}
	
	public static void print(Object currentObject) {
		logger.info(((ProfilingTimerBean) currentObject).getPrintable(""));
	}
	
	public static String get(Object currentObject) {
		return String.valueOf(((ProfilingTimerBean) currentObject).totalTime);
	}

	public static boolean isActive() throws IOException {
		// TODO Auto-generated method stub
		return (getActivateProfiling() == null) ? false : getActivateProfiling().equals("Y");
	}
	
	public static String getActivateProfiling() throws IOException {
		return AppProperties.getProperty("utilProfilingStack");
	}
}
