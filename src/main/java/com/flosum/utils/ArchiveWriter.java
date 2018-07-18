package com.flosum.utils;

import java.io.PrintWriter;
import java.io.Writer;
import java.io.OutputStream;
import java.lang.StringBuilder;

/**
 * Used to write output both to string and system.out
 */
public class ArchiveWriter extends PrintWriter {

	private static StringBuilder newData = new StringBuilder();
	// private static String newData;
	private static Boolean isChanged = false;

	public ArchiveWriter(Writer out) {
		super(out);
	}

	public ArchiveWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public ArchiveWriter(OutputStream out) {
		super(out);
	}

	public ArchiveWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		super.write(cbuf, off, len);
		newData.append(cbuf, off, len);
		isChanged = true;
	}

	@Override
	public void write(String s, int off, int len) {
		super.write(s, off, len);
		newData.append(s, off, len);
		isChanged = true;
	}

	public String getNewData() {
		isChanged = false;
		String checkOut = newData.toString().replace('\r', '\n');
		newData.setLength(0);// erase old string after read
		return checkOut;
	}

	public Boolean isNewData() {
		return isChanged;
	}
}