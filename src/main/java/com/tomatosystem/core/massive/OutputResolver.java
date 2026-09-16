package com.tomatosystem.core.massive;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

public class OutputResolver {
	private PrintWriter out = null;
	public OutputResolver(Writer out) {
		this.out = (PrintWriter)out;
	}
	
	public OutputResolver append(CharSequence v) {
		if (v != null)
			this.out.print(v.toString());
		return this;
	}

	public OutputResolver append(Number v) {
		if (v != null)
			this.out.print(v.toString());
		return this;
	}

	public OutputResolver append(Date v) {
		if (v != null)
			this.out.print(v.toString());
		return this;
	}

	public OutputResolver append(Object v) {
		if (v != null)
			this.out.print(String.valueOf(v));
		return this;
	}

	public OutputResolver append(boolean v) {
		this.out.print(String.valueOf(v));
		return this;
	}
	public OutputResolver append(byte v) {
		this.out.print(String.valueOf(v));
		return this;
	}
	public OutputResolver append(char v) {
		this.out.print(String.valueOf(v));
		return this;
	}
	public OutputResolver append(short v) {
		this.out.print(String.valueOf(v));
		return this;
	}
	public OutputResolver append(int v) {
		this.out.print(String.valueOf(v));
		return this;
	}
	public OutputResolver append(long v) {
		this.out.print(String.valueOf(v));
		return this;
	}
	public OutputResolver append(double v) {
		this.out.print(String.valueOf(v));
		return this;
	}
	public OutputResolver append(float v) {
		this.out.print(String.valueOf(v));
		return this;
	}
	
	public void flush(){
		this.out.flush();
	}
}
