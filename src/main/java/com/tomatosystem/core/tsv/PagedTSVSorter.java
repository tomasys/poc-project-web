package com.tomatosystem.core.tsv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PagedTSVSorter {
	List<List<String>> fData;
	int fSortIndex = 0;
	int [] fArrSortIndex = {};
	boolean [] fIsAsc = {};
	boolean[] fIsColumnNumType = {};
	boolean fAsc = true;
	boolean fNumericCompare = false;
	
	/**
	 * 페이징된 TSV를 정렬하는 소터를 만듭니다.
	 * 
	 * @param sortIndex
	 *            정렬할 컬럼 인덱스.
	 * @param asc
	 *            오름차순 정렬 여부.
	 */
	PagedTSVSorter(int sortIndex, boolean numericCompare, boolean asc) {
		fSortIndex = sortIndex;
		fAsc = asc;
		fNumericCompare = numericCompare;
		fData = new ArrayList<List<String>>();
	}
	
	/**
	 * 페이징된 TSV를 정렬하는 소터를 만듭니다.
	 * 
	 * @param sortIndex
	 *            정렬할 컬럼 인덱스.
	 * @param asc
	 *            오름차순 정렬 여부.
	 */
	PagedTSVSorter(int[] arrSortIndex, boolean[] isAsc, boolean[] isColumnNumType, int sortCol, boolean numericCompare, boolean asc) {
		fArrSortIndex = arrSortIndex;
		fIsAsc = isAsc;
		fSortIndex = sortCol;
		fIsColumnNumType = isColumnNumType;
		fAsc = asc;
		fNumericCompare = numericCompare;
		fData = new ArrayList<List<String>>();
	}

	private int compareCell(String a, String b) {
		String left = fAsc ? a : b;
		String right = fAsc ? b : a;
		if (fNumericCompare) {
			return Double.compare(parseDouble(left), parseDouble(right));
		} else {
			return (left != null ? left : "").compareTo(right != null ? right : "");
		}
	}

	private double parseDouble(String text) {
		try {
			return Double.parseDouble(text);
		} catch (Exception e) {
			return 0.0d;
		}
	}

	/**
	 * 행을 추가 합니다.
	 * 
	 * @param line
	 *            행.
	 */
	void add(String line) {
		String[] cells = line.split("\t");
		fData.add(Arrays.asList(cells));
	}

	/**
	 * 추가된 모든 행들을 정렬하고 {@link PagedTSV}로 빌드 합니다.
	 * 
	 * @param cacheId
	 *            빌드된 페이징 TSV의 식별자.
	 * @return 정렬된 {@link PagedTSV}.
	 * @throws IOException
	 */
	PagedTSV build(String cacheId) throws IOException {
		PagedTSV pagedTSV = new PagedTSV(cacheId);
		PagedTSVWriter writer = pagedTSV.openWriter();
		
		Comparator<List<String>> comparator = createComparator(fArrSortIndex, fIsAsc, fIsColumnNumType, 0);
		fData.sort(comparator);
		
//		fData.sort(new Comparator<List<String>>() {
//			@Override
//			public int compare(List<String> o1, List<String> o2) {
//				return compareCell(o1.get(fSortIndex), o2.get(fSortIndex));
//			}
//		});
		
		Iterator<List<String>> iterator = fData.iterator();
		while (iterator.hasNext()) {
			List<String> line = iterator.next();
			writer.beginNewLine();
			for (String cell : line) {
				writer.writCellValue(cell);
			}
		}
		writer.close();
		pagedTSV.setRowCount(writer.getLineCount());
		pagedTSV.saveManifest();

		return pagedTSV;
	}
	
//	public static Comparator<List<String>> createComparator(int[] columns, boolean[] ascending, int index) {
//        if (index >= columns.length) {
//            return (row1, row2) -> 0; // 마지막에 도달하면 더 이상 비교하지 않음
//        }
//        
//        // 현재 컬럼에 대한 Comparator 생성
//        Comparator<List<String>> currentComparator = Comparator.comparing(row -> row.get(columns[index]));
//
//        // 오름차순 또는 내림차순 설정
//        if (!ascending[index]) {
//            currentComparator = currentComparator.reversed();
//        }
//
//        // 다음 컬럼에 대한 Comparator 생성 및 체인 연결
//        return currentComparator.thenComparing(createComparator(columns, ascending, index + 1));
//    }
	
	public static Comparator<List<String>> createComparator(int[] columns, boolean[] isAscending, boolean[] isNumeric, int index) {
        if (index >= columns.length) {
            return (row1, row2) -> 0; // 마지막에 도달하면 더 이상 비교하지 않음
        }
        
        // 현재 컬럼에 대한 Comparator 생성
        Comparator<List<String>> currentComparator;
        
        if (isNumeric[index]) {
        	
        	try {
        		currentComparator = Comparator.comparingInt(row -> Integer.parseInt(row.get(columns[index])));
        	}catch (Exception e) {
        		currentComparator = Comparator.comparing(row -> row.get(columns[index]));
			}finally {
				//currentComparator = Comparator.comparing(row -> row.get(columns[index]));
			}
            
        } else {
            currentComparator = Comparator.comparing(row -> row.get(columns[index]));
        }

        // 오름차순 또는 내림차순 설정
        if (!isAscending[index]) {
            currentComparator = currentComparator.reversed();
        }

        // 다음 컬럼에 대한 Comparator 생성 및 체인 연결
        return currentComparator.thenComparing(createComparator(columns, isAscending, isNumeric, index + 1));
    }
}
