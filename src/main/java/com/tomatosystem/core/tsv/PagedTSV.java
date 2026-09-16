package com.tomatosystem.core.tsv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class PagedTSV {
	static final int DEFAULT_PAGE_SIZE = 5000;
	private static final Logger logger = LogManager.getLogger(PagedTSV.class);
	private static File getCacheDirectory() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File cacheDir = new File(tempDir, "xml-cache");
		if (cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return cacheDir;
	}

	static {
		File[] caches = getCacheDirectory().listFiles();
		if (caches != null) {
			for (File f : caches) {
				clean(f);
			}
		}
	}

	private static void clean(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				try {
					clean(f);
				} catch (Exception e) {

				}
			}

		}

		try {
			file.delete();
		} catch (Exception e) {

		}
	}

	public String fCacheId;
	String[] fColumnTypes;
	int fPageSize = -1;
	int fRowCount = -1;

	Properties fMenifest;

	/**
	 * 디스크에 이미 존재하는 페이징 TSV를 사용하기 위해 {@linkplain PagedTSV}를 만듭니다.
	 * 
	 * @param cacheId
	 *            식별자.
	 */
	public PagedTSV(String cacheId) {
		fCacheId = cacheId;
		Properties manifest = getManifest();
		loadManifiest(manifest);
	}
	
	/**
	 * 신규 {@linkplain PagedTSV}를 만듭니다. 만일 주어진 식별자에 해당하는 페이징 TSV가 이미 존재한다면, 해당
	 * 정보가 우선 취급 됩니다.
	 * 
	 * @param cacheId
	 *            식별자.
	 * @param pageSize
	 *            단일 페이지가 포함하는 행의 개수.
	 */
	public PagedTSV(String cacheId, String[] columnTypes, int pageSize) {
		fCacheId = cacheId;

		Properties manifest = getManifest();
		loadManifiest(manifest);

		fColumnTypes = columnTypes;
		fPageSize = pageSize;
	}

	/**
	 * @return 페이징 TSV가 디스크에 이미 존재하는지 여부.
	 */
	public boolean exists() {
		return getManifestFile().exists();
	}

	/**
	 * 식별자를 얻습니다. 식별자는 캐시 디렉토리 경로를 결정하는 데 사용됩니다.
	 * 
	 * @return 식별자
	 */
	public String getCacheId() {
		return fCacheId;
	}

	public String[] getColumnTypes() {
		return fColumnTypes;
	}
	
	public String getColumnType(int colIdx) {
		if(fColumnTypes == null){
			return null;
		}
		if(colIdx < 0 || colIdx >= fColumnTypes.length) {
			return null;
		}
		return fColumnTypes[colIdx];
	}

	public void setColumnTypes(String[] columnTypes) {
		fColumnTypes = columnTypes;
	}

	/**
	 * 페이징 TSV에 대한 쓰기 스트림을 얻습니다. 쓰기 스트림은 항상 0페이지 0행 부터 씁니다. 가장 첫번째 행이라 할지라도
	 * {@link PagedTSVWriter#beginNewLine()} 으로 새 행을 시작 해야 합니다.
	 * 
	 * @return 쓰기 스트림.
	 */
	public PagedTSVWriter openWriter() {
		if (fPageSize == -1) {
			throw new RuntimeException("페이지 크기가 확정되지 않았습니다.");
		}
		return new PagedTSVWriter(getWorkDir(), "page", fPageSize);
	}

	/**
	 * 페이징 TSV에 대한 읽기 스트림을 얻습니다. 이 스트림은
	 * {@link PagedTSVReader#pipeTo(java.io.Writer, int, int)}를 이용하여 다른 쓰기 스트림에
	 * 페이징 TSV의 특정 행부터 특정 길이까지의 행들을 모두 전달 할 수 있습니다.
	 * 
	 * @return 읽기 스트림.
	 */
	public PagedTSVReader openReader() {
		if (fPageSize == -1) {
			throw new RuntimeException("페이지 크기가 확정되지 않았습니다.");
		}
		return new PagedTSVReader(getWorkDir(), "page", fPageSize);
	}

	/**
	 * 전체 행의 개수를 지정합니다. 새로 작성한 경우, 길이 정보를 저장해 두는 용도로 사용 됩니다.
	 * 
	 * @param rowCount
	 *            전체 행의 개수.
	 * @throws IOException
	 */
	public void setRowCount(int rowCount) throws IOException {
		fRowCount = rowCount;
	}

	/**
	 * 전체 행의 갯수를 얻습니다. <code>-1</code>인 경우 아직 지정되지 않은 상태 입니다.
	 * 
	 * @return 전체 행의 개수.
	 */
	public int getRowCount() {
		return fRowCount;
	}

	/**
	 * 한 페이지가 포함할 행의 개수를 지정합니다.
	 * 
	 * @param pageSize
	 *            한 페이지가 포함할 행의 개수.
	 * @throws IOException
	 */
	public void setPageSize(int pageSize) throws IOException {
		fPageSize = pageSize;
	}

	/**
	 * @return 한 페이지가 포함하는 행의 개수.
	 */
	public int getPageSize() {
		return fPageSize;
	}

	/**
	 * 매니페스트(행의 갯수, 페이지 크기, 컬럼 타입을)를 저장합니다.
	 * 
	 * @throws IOException
	 */
	public void saveManifest() throws IOException {
		Properties manifest = getManifest();
		manifest.setProperty("pageSize", "" + fPageSize);
		manifest.setProperty("rowCount", "" + fRowCount);
		if (fColumnTypes != null && fColumnTypes.length > 0) {
			manifest.setProperty("columnTypes", String.join(",", fColumnTypes));
		}
		manifest.store(new FileOutputStream(getManifestFile()), "cache");
		try {
			logger.debug("매니페스트 설정(행의 갯수, 페이지 크기, 컬럼 타입을) 저장");
			logger.debug("pageSize : " + String.valueOf(fPageSize));
			logger.debug("rowCount : " + String.valueOf(fRowCount));
			logger.debug("dataset columnTypes : " + fColumnTypes!=null ? String.join(",", fColumnTypes) : "");
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

	private File getManifestFile() {
		File file = new File(getWorkDir(), "manifest.properties");
		return file;
	}

	public File getWorkDir() {
		File dir = new File(getCacheDirectory(), fCacheId);
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		return dir;
	}

	private Properties getManifest() {
		if (fMenifest == null) {
			File file = new File(getWorkDir(), "manifest.properties");
			fMenifest = new Properties();
			if (file.exists()) {
				try {
					fMenifest.load(new FileInputStream(file));
				} catch (IOException e) {
					return null;
				}
			}

		}

		return fMenifest;
	}
	
	/**
	 * 정렬된 {@link PagedTSV}를 얻습니다.
	 * 
	 * @param sortCol
	 *            정렬할 컬럼 인덱스. (0 베이스)
	 * @param asc
	 *            오름차순 여부.
	 * @return 정렬된 새로운 {@link PagedTSV}.
	 * @throws IOException
	 */
	public PagedTSV getSorted(int sortCol, boolean asc) throws IOException {
		String sortedCacheId = String.format("%s-sort-%03d-%s", this.getCacheId(), sortCol, asc ? "asc" : "desc");
		PagedTSV existing = new PagedTSV(sortedCacheId, getColumnTypes(), getPageSize());
		if (existing.exists()) {
			return existing;
		}
		
		String columnType = getColumnType(sortCol);
		PagedTSVSorter sorter = new PagedTSVSorter(
			// 소트 컬럼 인덱스
			sortCol,
			// 숫자형 비교 여부 
			columnType != null && columnType.equals("number"),
			// 오름차순 여부
			asc
		);
		LineStream stream = LineStream.withConsumer(line -> {
			sorter.add(line);
		});
		this.openReader().pipeTo(stream, 0, this.getRowCount());

		// 스트림에 남은 마지막 행 처리.
		stream.close();

		return sorter.build(sortedCacheId);
	}

	
	/**
	 * 정렬된 {@link PagedTSV}를 얻습니다.
	 * 
	 * @param sortCol
	 *            정렬할 컬럼 인덱스. (0 베이스)
	 * @param asc
	 *            오름차순 여부.
	 * @return 정렬된 새로운 {@link PagedTSV}.
	 * @throws IOException
	 */
	public PagedTSV getSorted(int[] intColumns, boolean[] isAsc, boolean[] isColumnNumType, int sortCol, boolean asc) throws IOException {
		
//		String sortedCacheId = String.format("%s-sort-%03d-%s", this.getCacheId(), sortCol, asc ? "asc" : "desc");
		
		StringBuilder sbAsc = new StringBuilder();
        for (boolean b : isAsc) {
            if (sbAsc.length() > 0) {
            	sbAsc.append(","); // 구분자 추가
            }
            sbAsc.append(b);
        }
		String sortedCacheId = String.format("%s-sort-%s-%s", this.getCacheId(), Arrays.toString(intColumns), sbAsc);
//		System.out.println(sortedCacheId);
		
		PagedTSV existing = new PagedTSV(sortedCacheId, getColumnTypes(), getPageSize());
		if (existing.exists()) {
			return existing;
		}
		
		String columnType = getColumnType(sortCol);
		
		PagedTSVSorter sorter = new PagedTSVSorter(
			intColumns, 
			isAsc,
			isColumnNumType,
			// 소트 컬럼 인덱스
			sortCol,
			// 숫자형 비교 여부 
			columnType != null && columnType.equals("number"),
			// 오름차순 여부
			asc
		);
		LineStream stream = LineStream.withConsumer(line -> {
			sorter.add(line);
		});
		this.openReader().pipeTo(stream, 0, this.getRowCount());

		// 스트림에 남은 마지막 행 처리.
		stream.close();

		return sorter.build(sortedCacheId);
	}

	/**
	 * 디스크에 저장된 모든 정보를 파기 합니다.
	 */
	public void purge() {
		File[] files = getWorkDir().listFiles();
		if (files != null) {
			for (File f : files) {
				try {
					f.delete();
				} catch (Exception e) {

				}
			}
		}
	}

	private void loadManifiest(Properties manifest) {
		fRowCount = Integer.parseInt(manifest.getProperty("rowCount", "-1"));
		fPageSize = Integer.parseInt(manifest.getProperty("pageSize", "-1"));
		if (fPageSize < 0) {
			fPageSize = DEFAULT_PAGE_SIZE;
		}
		String typesExp = manifest.getProperty("columnTypes", null);
		if (typesExp != null && typesExp.length() > 0) {
			fColumnTypes = typesExp.split("[ ,]+");
		}
	}

}
