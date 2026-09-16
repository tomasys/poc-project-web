package com.tomatosystem.app.sample.service;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.protocol.data.ParameterRow;
import com.cleopatra.protocol.data.UploadFile;
import com.tomatosystem.core.constants.Alert;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.resource.AppProperties;
import com.tomatosystem.core.service.AbstractService;
import com.tomatosystem.core.util.FileUtil;
import com.tomatosystem.core.util.StringUtil;

@Service
public class CmnFileService extends AbstractService {
	
	public List<Map<String, Object>> selectCmnFileList(Map<String, String> mapParam) throws Exception {
		return dao.selectList("cmn-base01.selectCmnFileList", mapParam);
	}
	
	public Map<String, Object> selectCmnFile(Map<String, String> mapParam) throws Exception {
		return dao.selectOne("cmn-base01.selectCmnFileList", mapParam);
	}
	
	public Map<String, String> uploadCmnFile(DataRequest dataRequest) throws Exception {
		
		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
		//프로그램별 파일업로드 경로 (uri 및 메뉴별 파일 저장도 별도 지정 필요)
		//appworks.properties 파일에 정의한다.
		String strFileStorePath = AppProperties.getProperty("cmn.fileStorePath");;
		//년월
//		strFileStorePath += "/"+ cmnCommonService.selectSysDate("%Y%m");
		
		if("".equals(StringUtil.fixNull(strFileStorePath))){
			//첨부파일을 저장할 저장소 경로가 존재하지 않습니다.
			throw new AppWorksException("첨부파일을 저장할 저장소 경로가 존재하지 않습니다.");
		}
		
		ParameterGroup dmParam = dataRequest.getParameterGroup("dmParam");
		
		String strAttcFileNo = StringUtil.fixNull(dmParam.getValue("strAttcFileNo"));//첨부파일번호
		//첨부번호가 없으면... 신규 업로드임
		String strFileStatRcd = "CMN101.SAVE";
		if("".equals(strAttcFileNo)){
			strAttcFileNo = getRandomString(20);
			strFileStatRcd = "CMN101.TEMP";
		}
		
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> fileInfo = new HashMap<String, String>();
		
		Map<String, UploadFile[]> uploadFiles = dataRequest.getUploadFiles();
		int iFileCnt = 0;
		if(uploadFiles != null && uploadFiles.size() > 0) {
			Set<Entry<String, UploadFile[]>> entries = uploadFiles.entrySet();
			for(Entry<String, UploadFile[]> entry : entries) {
				UploadFile[] uFiles = entry.getValue();
				for(UploadFile uFile : uFiles){
					File file = uFile.getFile();
					String strFileName = uFile.getFileName();				//파일명
					String strFileSize = Long.toString(file.length());		//파일 사이즈
					String strFileExt = FileUtil.getFileExtNm(strFileName);	//확장자명
					String strTempPath = file.getPath();					//임시 파일업로드 경로
					
					//보안에 위배되는 파일 확장자 유형인 경우...
//					if(!SecurityWebUtil.securedFileType(strFileName)){
//						//{0} 확장자는 업로드 할 수 없습니다.
//						throw new AppWorksException("CMN003.CMN@CMN021", Alert.ERROR, FileUtil.getFileExtNm(strFileName));
//					}
					
					//파일명에 대한 암호화
					String strSaveNameFileNm = FileUtil.getEncryptFileNm();
					strSaveNameFileNm = FileUtil.uploadFile(strGlobalFileStorePath+strFileStorePath, strTempPath, strSaveNameFileNm, true);
					
					//공통 첨부파일 DB테이블에 저장
					param.clear();
					param.put("ATTC_FILE_NO", strAttcFileNo);		//첨부파일 번호
					param.put("FILE_NM", strFileName);				//업로드 파일명
					param.put("SAVE_FILE_NM", strSaveNameFileNm);	//업로드 저장 파일명(암호화된 파일)
					param.put("FILE_PATH", strFileStorePath);		//업로드 파일 저장경로
					param.put("FILE_EXT", strFileExt);				//파일 확장자
					param.put("FILE_SIZE", strFileSize);			//파일 사이즈
					param.put("FILE_STAT_RCD", strFileStatRcd);		//파일 저장상태[CMN101]
					
					//데이터 저장
					this.insertCmnFile(param);
					
					iFileCnt++;
					fileInfo.put("attcFileNo", strAttcFileNo);
					fileInfo.put("fileNm", strFileName);
					fileInfo.put("fileSize", strFileSize);
				}
			}
		}
		
		fileInfo.put("fileCnt", Integer.toString(iFileCnt));
		
		return fileInfo;
	}

	
	public int insertCmnFile(Map<String, String> mapParam) throws Exception {
		return dao.insert("cmn-base01.insertCmnFile", mapParam);
	}
	
	
	public int commitCmnFile(String strAcctFileNo) throws Exception {
		if(!"".equals(StringUtil.fixNull(strAcctFileNo))){
			Map<String, String> mapParam = new HashMap<String, String>();
			mapParam.put("ATTC_FILE_NO", strAcctFileNo);
			
			return dao.insert("cmn-base01.commitCmnFile", mapParam);
		}
		
		return 0;
	}
	
	
	public int commitCmnFile(String strAcctFileNo, String strOriAcctFileNo) throws Exception {
		if(strAcctFileNo != null && !"".equals(strAcctFileNo) && !strAcctFileNo.equals(strOriAcctFileNo)){
			Map<String, String> mapParam = new HashMap<String, String>();
			mapParam.put("ATTC_FILE_NO", strAcctFileNo);
			
			return dao.insert("cmn-base01.commitCmnFile", mapParam);
		}
		
		return 0;
	}

	
	public int deleteCmnFile(ParameterGroup dsFiles) throws Exception {
		Iterator<ParameterRow> deletedRows = dsFiles.getDeletedRows();
		
		int result = 0;
		Map<String, String> param = null;
		while(deletedRows.hasNext()){
			param = deletedRows.next().toMap();
			
			result += deleteCmnFileByAttcFileNo(param);
		}
		
		return result;
	}

	
	public int deleteCmnFileByAttcFileNo(Map<String, String> mapParam) throws Exception {
		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
		
		List<Map<String, Object>> fileList = null;
		int result = 0;
		//1. 첨부파일 정보 조회
		fileList = selectCmnFileList(mapParam);
		if(fileList != null){
			for(Map<String, Object> file : fileList){
				if(file != null && file.size() > 0){
					file.put("ATTC_FILE_NO__origin", file.get("ATTC_FILE_NO"));
					file.put("SEQ__origin", file.get("SEQ"));
					
					//2. DB 첨부파일 정보 삭제
					result += dao.delete("cmn-base01.deleteCmnAttcFile", file);
					
					//3. 스토리지에 있는 실제 파일 삭제
					if(file != null && file.size() > 0){
						String strDeleteFilePath = strGlobalFileStorePath;
						strDeleteFilePath += file.get("FILE_PATH");
						if(strDeleteFilePath.toString().indexOf("../") != -1){
							//잘못된 첨부파일 경로입니다.(보안상의 이유로 상위폴더에 대한 접근은 불가합니다
							throw new AppWorksException("CMN003.CMN@CMN031", Alert.WARN);
						}
						
						FileUtil.deleteFile(strDeleteFilePath+"/"+file.get("SAVE_FILE_NM"));
					}
				}
			}
		}
		
		return result;
	}
	private static String dummyString="1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijlmnopqrstuvwxyz";
	private static Random random = new Random();
	public static String getRandomString(int loopCount) {

		StringBuilder tempBuilder=new StringBuilder(100);
		int randomInt;
		char tempChar; 

		for(int loop=0;loop<loopCount;loop++) 
		{
			randomInt=random.nextInt(61);
			tempChar=dummyString.charAt(randomInt);
			tempBuilder.append(tempChar);
		}
		return tempBuilder.toString();
	}
}