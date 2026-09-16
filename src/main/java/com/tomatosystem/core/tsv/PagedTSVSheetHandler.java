package com.tomatosystem.core.tsv;

import java.io.IOException;

import org.apache.poi.xssf.binary.XSSFBSheetHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;

public class PagedTSVSheetHandler implements SheetContentsHandler {
	PagedTSVWriter fPagedTSVWriter;
	int fIgnoreCount = 0;

	public PagedTSVSheetHandler(PagedTSVWriter writer, int headerLines) {
		fPagedTSVWriter = writer;
		fIgnoreCount = headerLines;
	}

	@Override
	public void cell(String columnName, String value, XSSFComment comment) {
		if (fIgnoreCount > 0) {
			return;
		}
		try {
			fPagedTSVWriter.writCellValue(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void endRow(int arg0) {
		if (fIgnoreCount > 0) {
			fIgnoreCount--;
		}
	}

	@Override
	public void startRow(int arg0) {
		if (fIgnoreCount > 0) {
			return;
		}
		try {
			fPagedTSVWriter.beginNewLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void hyperlinkCell(String arg0, String arg1, String arg2, String arg3, XSSFComment arg4) {

	}
}
