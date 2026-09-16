package com.tomatosystem.core.profiler.timer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProfilingTimerBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List children = new ArrayList();
	
	private ProfilingTimerBean parent = null;
	
	private String resuorce;
	
	public long startTime;
	public long totalTime;
	
	public ProfilingTimerBean(String resuorce) {
		this.resuorce = resuorce;
	}
	
	public void addParent(ProfilingTimerBean parent) {
		this.parent = parent;
	}
	
	public ProfilingTimerBean getParent() {
		return this.parent;
	}
	
	public void addChild(ProfilingTimerBean child) {
		children.add(child);
		child.addParent(this);
	}
	
	public void setStartTime() {
		this.startTime = System.currentTimeMillis();
	}
	
	public void setEndTime() {
		this.totalTime = System.currentTimeMillis() - this.startTime;
	}
	
	public String getPrintable(String indent) {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(indent + new ProfilingDataBean("["+ this.totalTime + "ms] - " + this.getResource()) +"\n");
		
		Iterator childrenIt = children.iterator();
		while (childrenIt.hasNext()) {
			ProfilingTimerBean timerBean = (ProfilingTimerBean) childrenIt.next();
			buffer.append(timerBean.getPrintable(indent + "  "));
		}
		
		return buffer.toString();
	}
	
	public String getResource() {
		return this.resuorce;
	}
}
