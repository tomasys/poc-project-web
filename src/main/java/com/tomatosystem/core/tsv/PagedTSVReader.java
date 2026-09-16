package com.tomatosystem.core.tsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.stream.Stream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tomatosystem.core.profiler.UtilProfilingStack;

/**
 * 페이지된 TSV 파일들이 있는 디렉토리를 읽어 들이는 리더. {@link #pipeTo(Writer, int, int)}를 이용하여 다른
 * 라이터에 연결할 수 있습니다.
 * 
 * @author jeeeyul
 *
 */
public class PagedTSVReader {
	
	private static final Logger logger = LogManager.getLogger(PagedTSVReader.class);
	
	private int fPageSize = 5000;
	
	private int fPageLogSize = 200000;

	/** 작업 디렉토리 */
	private File fWorkDir;

	/** 각 페이징된 TSV 파일의 프리픽스 */
	private String fPrefix;

	/**
	 * @param workDir
	 *            페지징된 TSV가 존재하는 디렉토리.
	 * @param filePrefix
	 *            각 페이지 파일의 프리픽스.
	 * @param pageSize
	 *            각 페이지 파일의 크기. (행의 개수)
	 */
	public PagedTSVReader(File workDir, String filePrefix, int pageSize) {
		fWorkDir = workDir;
		this.fPrefix = filePrefix;
		fPageSize = pageSize;
	}
	
	public PagedTSVReader() {
	}
	public void pipeTo(Writer writer, int rowStart, int rowCount) throws IOException {
		int rowEnd = rowStart + rowCount - 1;
		int pageStart = rowStart / fPageSize;
		int pageEnd = rowEnd / fPageSize;

		Looping: for (int page = pageStart; page <= pageEnd; page++) {
			String pageFilename = String.format("%s_%06d", fPrefix, page);
			File file = new File(fWorkDir, pageFilename);
			if (file.exists() == false) {
				break Looping;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//			logger.debug(pageFilename + " file read");
			try {
				int rowIdx = page * fPageSize;

				Stream<String> stream = reader.lines();
				if (rowStart > rowIdx) {
					int skipCount = rowStart - rowIdx;
					stream = stream.skip(skipCount);
					rowIdx += skipCount;
				}

				if (pageEnd == page) {
					stream = stream.limit(rowEnd - rowIdx + 1);
				}

				stream.forEach(e -> {
					try {
						writer.append(e);
						writer.append('\n');
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

			finally {
				reader.close();
			}

		}

	}
	
	public void pipeToLog(Writer writer, int rowStart, int rowCount, String logFilename) throws IOException {
		int rowEnd = rowStart + rowCount - 1;
		int pageStart = rowStart / fPageLogSize;
		int pageEnd = rowEnd / fPageLogSize;
		
		Looping: for (int page = pageStart; page <= pageEnd; page++) {
			String pageFilename = String.format("%s_%06d", fPrefix, page);
			File file = new File(logFilename);
			if (file.exists() == false) {
				break Looping;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//			logger.debug(pageFilename + " file read");
			try {
				int rowIdx = page * fPageSize;

				Stream<String> stream = reader.lines();
				if (rowStart > rowIdx) {
					int skipCount = rowStart - rowIdx;
					stream = stream.skip(skipCount);
					rowIdx += skipCount;
				}

				if (pageEnd == page) {
					stream = stream.limit(rowEnd - rowIdx + 1);
				}

				stream.forEach(e -> {
					try {
						writer.append(e);
						writer.append('\n');
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

			finally {
				reader.close();
			}

		}

	}
	public void pipeTo(File file, Writer writer) throws IOException {
		
		UtilProfilingStack.push(this.getClass().toString() + " : pipeTo");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		try {

			Stream<String> stream = reader.lines();
			stream.forEach(e -> {
				try {
					writer.append(e);
					writer.append('\n');
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
			finally {
				reader.close();
				UtilProfilingStack.pop(this.getClass().toString() + " : pipeTo");
			}
		}
	}
