package com.tomatosystem.core.tsv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cleopatra.type.TypeHandler;

public class PagedTSVWriter {
	
	private static final Logger logger = LogManager.getLogger(PagedTSVWriter.class);
	
	private int fPageSize = 5000;

	/** 작업 디렉토리 */
	private File fWorkDir;

	/** 각 페이징된 TSV 파일의 프리픽스 */
	private String filePrefix;

	private int fCurrentLineNumber = -1;
	private Writer fCurrentWriter;
	private boolean fNeedsLineFeed = false;
	private boolean fNeedsTab = false;

	private boolean fClosed = false;

	PagedTSVWriter(File workDir, String filePrefix, int pageSize) {
		fWorkDir = workDir;
		this.filePrefix = filePrefix;
		fPageSize = pageSize;
	}

	/**
	 * 새로운 행을 시작 합니다.
	 * @throws IOException
	 */
	public void beginNewLine() throws IOException {
		assertNotClosed();
		fCurrentLineNumber++;

		if (fCurrentLineNumber % fPageSize == 0) {
			closeExistingPageWriter();
			openCurrentPageWriter();
		}

		if (fNeedsLineFeed) {
			fCurrentWriter.append('\n');
			fNeedsLineFeed = false;
		}

		fNeedsLineFeed = false;
		fNeedsTab = false;
	}

	/**
	 * 셀 값을 추가 합니다.
	 * 
	 * @param value
	 *            추가할 값.
	 * @throws IOException
	 */
	public void writCellValue(String value) throws IOException {
		assertNotClosed();
		if (fNeedsTab) {
			fCurrentWriter.write('\t');
		}

		fCurrentWriter.write(encodeTSV(value));
		fNeedsTab = true;
		fNeedsLineFeed = true;
	}

	/**
	 * 기록을 종료하고 스트림을 닫습니다.
	 */
	public void close() {
		if (fClosed) {
			return;
		}
		fClosed = true;
		try {
			closeExistingPageWriter();
		} catch (IOException e) {
			// 무시.
		}

	}

	private void assertNotClosed() throws IOException {
		if (fClosed) {
			throw new IOException("PagesTSVFileWriter is closed!");
		}
	}

	private void closeExistingPageWriter() throws IOException {
		if (fCurrentWriter != null) {
			fCurrentWriter.close();
			fCurrentWriter = null;
		}
	}

	private void openCurrentPageWriter() throws IOException {
		String pageFilename = String.format("%s_%06d", filePrefix, fCurrentLineNumber / fPageSize);
		FileOutputStream fos = new FileOutputStream(new File(fWorkDir, pageFilename));
		fCurrentWriter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"), 16384);
		fNeedsLineFeed = false;
		fNeedsTab = false;
		logger.debug(pageFilename + " cache file writer");
//		System.out.println(fWorkDir + "\\" + pageFilename + " cache 파일 생성");
		
	}

	private String encodeTSV(Object value) {
		if (value == null) {
			return "";
		}
		String strValue = TypeHandler.toString(value);
		StringBuilder encodedValue = new StringBuilder();
		final int length = strValue.length();
		char ch = '\0';
		for (int i = 0; i < length; i++) {
			ch = strValue.charAt(i);
			if (ch == '\t' || ch == '\n' || ch == '\r' || ch == '\b' || ch == '\\') {
				encodedValue.append('\\'); // escape character
				if (ch == '\t') {
					encodedValue.append('t');
				} else if (ch == '\n') {
					encodedValue.append('n');
				} else if (ch == '\r') {
					encodedValue.append('r');
				} else if (ch == '\b') {
					encodedValue.append('b');
				} else {
					encodedValue.append('\\');
				}
			} else {
				encodedValue.append(ch);
			}
		}

		return encodedValue.toString();
	}

	/**
	 * 이 라이터를 통해 씌여진 모든 행의 수를 얻습니다,
	 * 
	 * @return 행의 수.
	 */
	public int getLineCount() {
		return fCurrentLineNumber + 1;
	}

}
