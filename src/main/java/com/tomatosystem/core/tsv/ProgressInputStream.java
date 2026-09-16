package com.tomatosystem.core.tsv;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.poi.openxml4j.util.ZipArchiveThresholdInputStream;

public class ProgressInputStream extends InputStream {
	static Map<String, ProgressInputStream> fTracingMap = new Hashtable<String, ProgressInputStream>();

	public static ProgressInputStream wrapWithKey(InputStream inputStream, String key) {
		ProgressInputStream result = new ProgressInputStream(key, inputStream);
		fTracingMap.put(key, result);
		return result;
	}
	
	public static ProgressInputStream wrapWithKey(InputStream inputStream, String key, int sheetIndex) {
		ProgressInputStream result = new ProgressInputStream(key, inputStream, sheetIndex);
		fTracingMap.put(key, result);
		return result;
	}
	
	public static void setPlaceholder(String key) {
		fTracingMap.put(key, new PlaceholderStream(key));
	}

	public static double getPercent(String progressId) {
		ProgressInputStream stream = fTracingMap.get(progressId);
		if (stream == null) {
			return -1.0d;
		} else {
			return stream.getProgress();
		}
	}
	
	public static int getSheetIndex(String progressId) {
		ProgressInputStream stream = fTracingMap.get(progressId);
		if (stream == null) {
			return 1;
		} else {
			return stream.getSheetIdx();
		}
	}
	
	long fTotalSize = -1;
	long fReadSize = 0;
	String fKey;
	InputStream fOriginStream;
	 int fSheetIndex = 1;

	private ProgressInputStream(String key, InputStream input) {
		fOriginStream = input;
		fKey = key;
		fTotalSize = detectSize(input);
	}
	
	private ProgressInputStream(String key, InputStream input, int sheetIndex) {
		fOriginStream = input;
		fKey = key;
		fTotalSize = detectSize(input);
		fSheetIndex = sheetIndex;
	}
	
	public double getProgress() {
		return fTotalSize <= 0 ? 0 : fReadSize / (double) fTotalSize;
	}
	
	public int getSheetIdx() {
		return fSheetIndex;
	}
	
	@Override
	public void close() throws IOException {
		fTracingMap.remove(fKey);
		super.close();
		fOriginStream.close();
	}

	@Override
	public int read() throws IOException {
		fReadSize++;
		return fOriginStream.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		fReadSize += len;
		return fOriginStream.read(b, off, len);
	}

	private long detectSize(InputStream is) {
		if (is instanceof ByteArrayInputStream) {
			try {
				Field countField = ByteArrayInputStream.class.getField("count");
				countField.setAccessible(true);
				return countField.getInt(is);
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		} else if (is instanceof ZipArchiveThresholdInputStream) {
			try {
				Field entryField = ZipArchiveThresholdInputStream.class.getDeclaredField("entry");
				entryField.setAccessible(true);
				ZipArchiveEntry entry = (ZipArchiveEntry) entryField.get(is);
				return entry.getSize();
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		} else if (is instanceof FileInputStream) {
			try {
				FileInputStream fileInputStream = (FileInputStream) is;
		        return fileInputStream.getChannel().size();
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
			
		} else if (is == null) {
			return 0;
		}
		else {
			System.err.println(is.getClass().getName() + "은 지원 안됩니다.");
			return -1;
		}
	}

	private static class PlaceholderStream extends ProgressInputStream {
		public PlaceholderStream(String key) {
			super(key, null);
		}

		@Override
		public double getProgress() {
			return 0;
		}
	}
}
