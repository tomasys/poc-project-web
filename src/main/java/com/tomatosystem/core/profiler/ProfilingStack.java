package com.tomatosystem.core.profiler;

public interface ProfilingStack {

	public void push(String name);
	
	public void pop(String name);
	
	public void print(Object currentObject);
	
	public boolean isActive();
}
