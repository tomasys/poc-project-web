package com.tomatosystem.core.profiler.timer;

import java.io.Serializable;

public class ProfilingDataBean implements Serializable {
	
	public String time;
	
	public ProfilingDataBean(String time) {
		this.time = time;
//		if ((JNDIHelper.getString("activate_database") == null) ? false : JNDIHelper.getString("activate_database").equals("Y")) {
//			init(time);
//		}
	}
	
	public void init(String time) {
		String[] profilingTimes = time.split("-");
		String profilingTime = profilingTimes[0].trim();
		String packageClassName = profilingTimes[1].trim();
		
//		String[] classNames = profilingTimes[1].split("#");
//		String[] packageClassNames = classNames[0].split(".");
		
//		String className = packageClassNames[packageClassNames.length];
//		String packageName = packageClassNames[0].substring(0, classNames[0].length()-className.length());
//		
//		profilingTime = profilingTime.substring(1, profilingTime.length()-4);
//		
//		System.out.println(profilingTime);
		System.out.println(profilingTime+" : "+packageClassName);
	}
	
	public String toString() {
		return this.time;
	}
}
