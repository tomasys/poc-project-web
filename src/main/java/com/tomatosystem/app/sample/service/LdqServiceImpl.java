package com.tomatosystem.app.sample.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.SAXParserFactory;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.cleopatra.json.JSONObject;
import com.cleopatra.protocol.builder.TSVResponseBuilder;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.DataResponse;
import com.tomatosystem.app.sample.mapper.LdqMapper;
import com.tomatosystem.core.massive.BufferedSendQueue;
import com.tomatosystem.core.massive.MassiveExcelResultHandler;
import com.tomatosystem.core.massive.ProgressExcelStore;
import com.tomatosystem.core.massive.StreamExcelResponseAdapter;
import com.tomatosystem.core.service.AbstractService;
import com.tomatosystem.core.tsv.PagedTSV;
import com.tomatosystem.core.tsv.PagedTSVWriter;
import com.tomatosystem.core.util.StringUtil;
import com.tomatosystem.core.util.UtilUuidMgr;

@Service
public class LdqServiceImpl extends AbstractService {

	private static int CACHE_SEQ = 0;
	private static final int BATCH_SIZE = 1000; // 한 번에 insert할 양
	
	private static DataResponse dataResponse = null;
	@Resource(name = "sqlSession")
	private SqlSessionFactory sqlSessionFactory;
	
	/*ldq*/
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void uploadLdqExcel(File file, org.json.simple.JSONObject poColInfo, HttpServletResponse response) throws Exception {

	    dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);

	    OPCPackage opcPackage = OPCPackage.open(file);
	    XSSFReader xssfReader = new XSSFReader(opcPackage);

	    StylesTable styles = xssfReader.getStylesTable();
	    ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);

	    SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
	    LdqMapper mapper = session.getMapper(LdqMapper.class);

	    int batchSize = BATCH_SIZE;

	    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
        
        DataFormatter formatter = new DataFormatter();

        ContentHandler handler = new XSSFSheetXMLHandler(
                styles,
                null,
                strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {

                    private Map<String, Object> rowMap;
                    private int currentRow = 0;
                    private int cellIndex = 0;
                    private int count = 0;

                    @Override
                    public void startRow(int rowNum) {
                        currentRow = rowNum;

                        if (rowNum == 0) return;

                        rowMap = new HashMap<>();
                        cellIndex = 0;
                    }

                    @Override
                    public void endRow(int rowNum) {

                        if (rowNum == 0) return;

                        try {
                            mapper.insertLdg(rowMap);
                            dataResponse.send(rowMap);
                            count++;

                            if (count % batchSize == 0) {
                                session.flushStatements();
                                session.commit();
                                logger.info("Inserted {} rows", count);
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void cell(String cellReference, String formattedValue, XSSFComment comment) {

                        if (currentRow == 0) return;

                        if (cellIndex <= 28) {
                            rowMap.put(getColumnName(cellIndex),
                                    formattedValue != null ? formattedValue.trim() : "");
                        }

                        cellIndex++;
                    }

                    @Override
                    public void headerFooter(String text, boolean isHeader, String tagName) {}
                },
                formatter,
                false
        );

	    xmlReader.setContentHandler(handler);

	    try (InputStream sheetStream = xssfReader.getSheetsData().next()) {
	    	xmlReader.parse(new InputSource(sheetStream));
	    }

	    session.flushStatements();
	    session.commit();
	    session.close();

	    if (dataResponse != null) dataResponse.finish();
	}
	private String getCellValue(Row row, int index, DataFormatter formatter) {
	    Cell cell = row.getCell(index);
	    if (cell == null) return "";
	    return formatter.formatCellValue(cell).trim();
	}
	private String getColumnName(int index) {

	    String[] columns = {
	    		"PRJCT_CD",
	    		"UP_CNSTKND_CD",
	    		"UP_CNSTKND_NM",
	    		"CNSTKND_ID",
	    		"CNSTKND_NM",
	    		"CBGT_DTLS_ID",
	    		"DTLSNM",
	    		"DTLS_SIZ",
	    		"DTLS_UNIT_NM",
	    		"CBGT_QTY",
	    		"CBGT_UNTPRC",
	    		"CBGT_AMT",
	    		"CBGT_MAT_UNTPRC",
	    		"CBGT_MAT_AMT",
	    		"CBGT_LAB_UNTPRC",
	    		"CBGT_LAB_AMT",
	    		"CBGT_OSORDR_MAT_UNTPRC",
	    		"CBGT_OSORDR_MAT_AMT",
	    		"CBGT_OSORDR_LAB_UNTPRC",
	    		"CBGT_OSORDR_LAB_AMT",
	    		"CBGT_OSORDR_EXPN_UNTPRC",
	    		"CBGT_OSORDR_EXPN_AMT",
	    		"CBGT_OSORDR_UNTPRC",
	    		"CBGT_OSORDR_AMT",
	    		"CBGT_EXPN_UNTPRC",
	    		"CBGT_EXPN_AMT",
	    		"ENFCM_EXPNDITM_CD",
	    		"ENFCM_IMPSBL_YN",
	    		"CNTRL_YN",
	    		"MAT_CD",
	    		"LN_CNST_YN",
	    		"WBS_CD",
	    		"STND_CD",
	    		"PRGSS_TRGT_PRMCT_YN",
	    		"QTY_CNTRL_YN",
	    		"DIRECT_CNSTCT_YN",
	    		"RMK",
	    		"GB_CD",
	    		"SORT_SERL",
	    		"RLACT_DTLS_ID",
	    		"ACCTCD",
	    		"EXPND_ITM_CD",
	    		"GAREA_YN",
	    		"FCLTY_NM"
	    };

	    return columns[index];
	}
	/*ldq*/
	public void selectLdqTsvList(Map<String, String> mapParam, HttpServletResponse response, final String progressId
			,Map<String, AtomicInteger> progressStore) throws Exception {
		
		final DataResponse dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);
		
		BufferedSendQueue sendQueue = new BufferedSendQueue(dataResponse, 5000, 1000); // 3000건 or 1초마다 flush

		try {
			ResultHandler<LinkedHashMap<String, Object>> resultHandler = new ResultHandler<LinkedHashMap<String, Object>>() {
//				private int processedRows = 0;
				@Override
				public void handleResult(ResultContext<? extends LinkedHashMap<String, Object>> resultContext) {
					
					LinkedHashMap<String, Object> row = resultContext.getResultObject();

					sendQueue.add(resultContext.getResultObject());
				}
			};
			dao.selectWithResultHandler("com.tomatosystem.app.sample.mapper.LdqMapper.selectLdgList", mapParam, resultHandler);
			sendQueue.flushRemaining(); // 마지막 남은 데이터 flush
		} finally {
			// 작업 완료 후 진행률 정보 제거
//			removeProgress(progressId, progressStore);
		}
	}
	
	
	public PagedTSV createTsvCacheFromDb(Map<String, Object> mapParam, final String progressId
			, Map<String, AtomicInteger> progressStore
			) throws Exception {

		final int totalRows = dao.selectOne("com.tomatosystem.app.sample.mapper.LdqMapper.selectLdgListCount", mapParam);

//		System.out.println("totalRows: " + totalRows);
		// 진행률 초기화
		progressStore.put(progressId, new AtomicInteger(0));

		String cacheId = String.format("tsv-%03d", CACHE_SEQ++); // java.util.UUID.randomUUID().toString());
//		System.out.println("cacheId: " + cacheId);
		String[] columnTypes = (String[]) mapParam.get("columnTypes");
		
		
		PagedTSV pagedTSV = new PagedTSV(cacheId, columnTypes, 5000); // 페이지당 5000줄
		pagedTSV.purge();
		final PagedTSVWriter tsvWriter = pagedTSV.openWriter();
		try {
		
			ResultHandler<LinkedHashMap<String, Object>> tsvResultHandler = new ResultHandler<LinkedHashMap<String, Object>>() {
				private List<String> headers = null;
				private int processedRows = 0;

				@Override
				public void handleResult(ResultContext<? extends LinkedHashMap<String, Object>> resultContext) {
					LinkedHashMap<String, Object> rowData = resultContext.getResultObject();
					try {
						if (headers == null) {
							headers = new ArrayList<>(rowData.keySet());
						}

						tsvWriter.beginNewLine(); 
						
						for (String header : headers) {
							Object value = rowData.get(header);
							tsvWriter.writCellValue(value != null ? value.toString() : "");
						}

					} catch (IOException e) {
						throw new RuntimeException("TSV 캐시 파일 작성 중 오류 발생", e);
					}

					processedRows++;
					if (processedRows % 10000 == 0 || processedRows == totalRows) { // 10000건마다 또는 마지막에 업데이트
						int percent = (int) (((double) processedRows / totalRows) * 100);
//						System.out.println("현재 몇번째 행: " + processedRows);
						progressStore.get(progressId).set(percent);
					}
				}
			};

			
			dao.selectWithResultHandler("com.tomatosystem.app.service.impl.LdgMapper.selectLdgList", mapParam, tsvResultHandler);

			
			pagedTSV.setRowCount(tsvWriter.getLineCount());
			pagedTSV.saveManifest();

			return pagedTSV;

		}

		finally {
			if (tsvWriter != null) {
				tsvWriter.close();
			}
			// 작업 완료 후 진행률 정보 제거
			removeProgress(progressId, progressStore);
		}
	}
	/*ldq*/
	public int getProgress(String progressId, Map<String, AtomicInteger> progressStore) {
		AtomicInteger progress = progressStore.get(progressId);
		
		return progress != null ? progress.get() : -1;
	}

	public void removeProgress(String progressId, Map<String, AtomicInteger> progressStore) {
		progressStore.remove(progressId);
	}
	/*ldq*/
	public void exportExcelToStream(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest, String sqlQuery)
			throws NoSuchMethodException, SecurityException, IOException {
		JSONObject data = dataRequest.getRequestObject();
		
		HttpSession session = request.getSession();
		
		String randomUUID = data.getString("excelKey");
		
		if(StringUtil.isNotNullEmpty(randomUUID)) {
			randomUUID = UtilUuidMgr.randomUUID().toString();
		}
		JSONObject jsonObject = data.getJSONObject("searchParam");
		Map<String, Object> searchParamMap = jsonObject.toMap();
		String progressId = (String) searchParamMap.get("progressId");
		// randomUUID로 관리
		StreamExcelResponseAdapter adapter = (StreamExcelResponseAdapter) session.getAttribute("export" + randomUUID);
		
		try {
			adapter = new StreamExcelResponseAdapter("export" + randomUUID, data);
			dao.selectWithResultHandler(sqlQuery, searchParamMap, new MassiveExcelResultHandler(adapter, progressId));
			adapter.endResponseMassive(response);
		}catch (Exception e) {
			logger.debug(e.getMessage());
		}finally {
			ProgressExcelStore.progressMap.remove(progressId);
			ProgressExcelStore.progressMap.remove(progressId+"_sheet");
		}
//		Map<String, Object> searchParam = (Map<String, Object>) data.getJSONObject("searchParam");
		
	}
	/*ldq*/
	public int getSelectLdqCntProgress(Map<String, String> mapParam) {
		// TODO Auto-generated method stub
		return dao.selectOne("com.tomatosystem.app.sample.mapper.LdqMapper.selectLdgListCount", mapParam);
	}
	
	

}
