package com.tomatosystem.app.sample.web;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.protocol.data.ParameterRow;
import com.cleopatra.protocol.data.UploadFile;
import com.cleopatra.spring.JSONDataView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomatosystem.app.sample.service.CmnFileService;
import com.tomatosystem.core.constants.Alert;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.resource.AppProperties;
import com.tomatosystem.core.util.FileUtil;
import com.tomatosystem.core.util.StringUtil;

/**
 * <pre>
 * 시  스  템  : 공통
 * 단위시스템  : 공통시스템
 * 프로그램명  : 파일업로드/다운로드
 * 설      명    : 첨부파일 업로드 및 파일 다운로드에 대한 요건은 프로젝트별 상이하므로 
 *                   해당 샘플 파일의 업로드 및 다운로드 기능만 참고하시길 바랍니다.
 * </pre>
 * 
 * 이력사항
 * 
 */
@Controller
@RequestMapping("/CmnFile")
public class CmnFileController {
	
	@Autowired
	private CmnFileService cmnFileService;
	
	/**
	 * 
	 * <pre>
	 * 메소드명	: list
	 * 설	 명	: 첨부파일번호로 첨부된 파일 리스트 조회
	 * </pre>
	 *
	 * 이력사항
	 * 2021. 6. 23. 
	 *
	 * @param request
	 * @param response
	 * @param dataRequest
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/list.do")
	public View list(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest, 
		Authentication authentication)
		throws Exception {
		
//		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
//		String strAttcFileNo = StringUtil.fixNull(param.getValue("strAttcFileNo"));
//		String strFileStatRcd = StringUtil.fixNull(param.getValue("strFileStatRcd"));
//		Map<String, String> mapParam = new HashMap<String, String>();
//		mapParam.put("ATTC_FILE_NO", strAttcFileNo);
//		mapParam.put("FILE_STAT_RCD", strFileStatRcd);
//		//첨부파일 조회
//		dataRequest.setResponse("dsFile", cmnFileService.selectCmnFileList(mapParam));
		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
		String strFileStorePath = AppProperties.getProperty("cmn.fileStorePath");
		
		if("".equals(StringUtil.fixNull(strFileStorePath))){
			//첨부파일을 저장할 저장소 경로가 존재하지 않습니다.
			throw new AppWorksException("첨부파일을 저장할 저장소 경로가 존재하지 않습니다.");
		}

        File directory = new File(strGlobalFileStorePath+strFileStorePath);
        List<Map<String,Object>> fileList = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                    	Map<String,Object> tempMap = new HashMap<String, Object>();
                    	tempMap.put("FILE_NM", file.getName());
                    	tempMap.put("FILE_PATH", strFileStorePath);
                    	tempMap.put("SAVE_FILE_NM", file.getName());
                    	tempMap.put("FILE_SIZE", file.length());
                        fileList.add(tempMap);                   
                    }
                }
            }
        }
        dataRequest.setResponse("dsFile",fileList);
//        return fileList;
		return new JSONDataView();
	}
	
	/**
	 * 파일을 업로드 처리한다.
	 * <pre>
	 * 메소드명	: upload
	 * 설	 명	: 업로드 기능만 참고하시길 바랍니다.
	 * </pre>
	 *
	 * 이력사항
	 *
	 * @param request
	 * @param response
	 * @param dataRequest
	 * @param requestData
	 * @param authentication
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/upload.do")
	public View upload(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest)
		throws Exception {
		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
		String strFileStorePath = AppProperties.getProperty("cmn.fileStorePath");;
		if("".equals(StringUtil.fixNull(strFileStorePath))){
			//첨부파일을 저장할 저장소 경로가 존재하지 않습니다.
			throw new AppWorksException("첨부파일을 저장할 저장소 경로가 존재하지 않습니다.");
		}
		
		ParameterGroup dmParam = dataRequest.getParameterGroup("dmParam");
		
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
					FileUtil.uploadFile(strGlobalFileStorePath+strFileStorePath, strTempPath, strFileName, true);
				}
			}
		}
		
		Map<String, Object> meta = new HashMap<String, Object>();
		meta.put("uploadPath", strGlobalFileStorePath+strFileStorePath);
		dataRequest.setResponse("dmUpload", meta);
		
		return new JSONDataView();
	}
	
	/**
	 * <pre>
	 * 메소드명	: delete
	 * 설	 명	: 공통 첨부파일을 삭제한다.
	 * </pre>
	 *
	 * 이력사항
	 *
	 * @param request
	 * @param response
	 * @param requestData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/delete.do")
	public View delete(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest,
			Authentication authentication)
		throws Exception {
		
//		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
//		
//		String strAttcFileNo = StringUtil.fixNull(param.getValue("strAttcFileNo"));
//		String strFileSeq = StringUtil.fixNull(param.getValue("strFileSeq"));
//		
		ParameterGroup dsFile = dataRequest.getParameterGroup("dsFile");
		List<Map<String,String>> deletedRowList = dsFile.getDeletedRowList();
		ArrayList<String> deleteFileNm = new ArrayList<String>();
		
		for(int i=0; i<deletedRowList.size(); i++) {
			String fileNms = deletedRowList.get(i).get("FILE_NM");
			deleteFileNm.add(fileNms);
		}
//		//삭제할 데이터가 데이터셋으로 넘어온 경우
//		if(dsFile != null){
//			cmnFileService.deleteCmnFile(dsFile);
//		}else{
//			Map<String, String> mapParam = new HashMap<String, String>();
//			mapParam.put("ATTC_FILE_NO", strAttcFileNo);
//			mapParam.put("SEQ", strFileSeq);
//			
//			cmnFileService.deleteCmnFileByAttcFileNo(mapParam);
//		}
		
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

        File directory = new File(strGlobalFileStorePath+strFileStorePath);
        List<Map<String,Object>> fileList = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
//            	files[]
                for (File file : files) {
                    if (file.isFile()) {
                    	String name = file.getName();
                    	if(deleteFileNm.contains(name)) {
                    		file.delete();
                    	}
//                    	Map<String,Object> tempMap = new HashMap<String, Object>();
//                    	tempMap.put("FILE_NM", file.getName());
//                    	tempMap.put("FILE_SIZE", file.length());
//                        fileList.add(tempMap);                   
                    }
                }
            }
        }
//        return fileList;
		
		return new JSONDataView();
	}
	
	/**
	 * 파일 다운로드 하기전에... 해당 파일이 실제 존재하는지 체크한다.
	 * @param request
	 * @param response
	 * @param requestData
	 * @param authentication
	 * @return
	 * @throws Exception 
	 * @throws StdServiceException
	 */
	@RequestMapping("/checkFileExist.do")
	public View checkExist(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest)
		throws Exception {
		
		ParameterGroup param = dataRequest.getParameterGroup("dmParamDown");
		
		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
		
		String strFilePath = param.getValue("filePath");//다운로드받을 파일의 경로
		
		File file = new File(strGlobalFileStorePath+"/"+strFilePath);
		if(file.exists()){
			Map<String, Object> message = new HashMap<String, Object>();
			message.put("exist", "Y");
			dataRequest.setMetadata(true, message);
		}else{
			//첨부파일이 존재하지 않아, 다운로드가 불가합니다.
			throw new AppWorksException("첨부파일이 존재하지 않아, 다운로드가 불가합니다.", Alert.ERROR);
		}
		return new JSONDataView();
	}
	
	/**
	 * 파일을 다운로드 한다.
	 * <pre>
	 * 메소드명	: download
	 * 설	 명	:
	 * </pre>
	 *
	 * 이력사항
	 *
	 * @param request
	 * @param response
	 * @param requestData
	 * @param authentication
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/download.do")
	public void download(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest)
		throws Exception {
		
		ParameterGroup param = dataRequest.getParameterGroup("dmParamDown");
		
		String strFilePath = StringUtil.fixNull(param.getValue("filePath"));//다운로드받을 파일의 경로
		String strFileNm = StringUtil.fixNull(param.getValue("fileNm")); //실제 파일명
		String strResType = StringUtil.fixNull(param.getValue("resType")); //서브미션 responseType
		
		//파일명이 없는 경우... 파일경로에서 파일명을 추출한다.
		if(strFileNm == null || "".equals(strFileNm)){
			int index = strFilePath.lastIndexOf(File.separator);
			if(index == -1){
				index = strFilePath.lastIndexOf("/");
			}
			strFileNm = strFilePath.substring(index+1);
		}

		try {
			String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
			FileUtil.fileDownloadWrapper(strGlobalFileStorePath+"/"+strFilePath, request, response, strFileNm);
		} catch (Exception e) {
			//ui의 responseType이 filedownload일 경우 브라우저에서 UI가 보여지는 영역과 별도의 iframe에서 동작하므로 아래와 같이 처리
			//blob일 경우 서버에서 발생하는 오류를 처리 할 수 없으므로 오류를 표시 할 수 있는 이미지를 다운로드 하게 응용 제어 필요 
			if(strResType.equals("filedownload")) {
				response.setContentType("text/html;charset=utf-8");
				response.setCharacterEncoding("utf-8");
				ServletOutputStream outs = response.getOutputStream();
				String errorMsg = "파일다운로드시 오류가 발생했습니다.\\n파일이 존재하지 않거나 네트워크가 불안정합니다.\\n관리자에게 문의바랍니다";
				String msg = new String(errorMsg.getBytes(), "8859_1");
			    try {
		            outs.println("<html><script type='text/javascript'>");
		            outs.println("alert(\"" + msg+ "\");");
		            outs.println("</script></html>");
		            outs.flush();
			    }finally {
			    	outs.close();
				}
			}
		} 
	} 
	
	
	
	@RequestMapping("/downloadAll.do")
	public View downloadAll(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest)
		throws Exception {
		
		Boolean isCheckFIleName = false;
		ParameterGroup dmParamDown = dataRequest.getParameterGroup("dmParamDown");
		String strFileName = dmParamDown.getValue("fileNm");
		if(dmParamDown != null) isCheckFIleName = true;
				
		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
		String strFileStorePath = AppProperties.getProperty("cmn.fileStorePath");
		String strAttcFileNo = StringUtil.fixNull(dataRequest.getParameter("strAttcFileNo"));
//		Map<String, String> mapParam = new HashMap<String, String>();
//		mapParam.put("ATTC_FILE_NO", strAttcFileNo);
		
//		List<Map<String, Object>> fileList = cmnFileService.selectCmnFileList(mapParam);
//		List<Map<String, Object>> fileList = new ArrayList<Map<String, Object>>();
		 File directory = new File(strGlobalFileStorePath+strFileStorePath);
	        List<Map<String,Object>> fileList = new ArrayList<>();

	        if (directory.exists() && directory.isDirectory()) {
	            File[] files = directory.listFiles();
	            if (files != null) {
	                for (File file : files) {
	                    if (file.isFile()) {
	                    	if(!isCheckFIleName || (isCheckFIleName && strFileName.indexOf(file.getName()) > -1)) {
	                    		Map<String,Object> tempMap = new HashMap<String, Object>();
	                    		tempMap.put("FILE_NM", file.getName());
	                    		tempMap.put("SAVE_FILE_NM", file.getName());
	                    		tempMap.put("FILE_PATH",strFileStorePath);
	                    		fileList.add(tempMap);                   
	                    	}
	                    }
	                }
	            }
	        }
		if(fileList != null && fileList.size() > 0){
			String fileName = (String)fileList.get(0).get("FILE_NM");
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			if(fileList.size() == 1){
				fileName += ".zip";
			}else{
				fileName += " 외("+(fileList.size()-1)+"개).zip";
			}
			
			//파일 다운로드 수행
			FileUtil.downloadAsZip(request, response, strGlobalFileStorePath, fileName, fileList);
		}
		
		return new JSONDataView();
	}
	
	/**
	 * 
	 * Method Name : fileDownLoad<BR/>
	 * Description : 파일다운로드		  <BR/>	
	 *
	 * @author      : Park. ju wan <BR/>
	 * History <BR/>     
	 * 2015. 10. 27. Park. ju wan 최초작성 <BR/>
	 *
	 * @param req
	 * @param resp
	 * @param dataView
	 * @param sqlClientAssists
	 * @param reqData
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/tmpDownload.do")
	public View tmpDownload(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest)
		throws Exception {
		
		String strTmpFilePath = dataRequest.getParameter("strTmpFilePath");	//템프폴더파일풀경로
		String strOriFileNm = dataRequest.getParameter("strOriFileNm");	//원(실)파일명
		//템프폴더에 저장된 경우
		if (StringUtil.isNotNullEmpty(strTmpFilePath)) {
			try{
				FileUtil.fileDownloadWrapper(strTmpFilePath, request, response, strOriFileNm);
			} finally {
				File file = new File(strTmpFilePath);
				if(file.exists()){
					file.delete();
				}
			}
		} else {
			String strFileNm = dataRequest.getParameter("strFileNm"); 			//저장된 파일명(파일명변환)
			String strOriFileNm2 = dataRequest.getParameter("strOriFileNm"); 	//원(실)파일명
			String strFileSubPath = dataRequest.getParameter("strFileSubPath");// 파일 서브경로
			//파일다운로드
			try {
				String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
				FileUtil.fileDownloadWrapper(strGlobalFileStorePath + File.pathSeparator + strFileSubPath
						+ File.pathSeparator + strOriFileNm2 , request, response, strFileNm);
				
			} catch (Exception e) {
				// 파일다운로드시 오류가 발생했습니다.\n파일이 존재하지 않거나 네트워크가 불안정합니다.\n관리자에게 문의바랍니다
				response.setContentType("text/html;charset=utf-8");
				response.setCharacterEncoding("utf-8");
				ServletOutputStream outs = response.getOutputStream();
				String errorMsg = "파일다운로드시 오류가 발생했습니다.\\n파일이 존재하지 않거나 네트워크가 불안정합니다.\\n관리자에게 문의바랍니다";
				String msg = new String(errorMsg.getBytes(), "8859_1");
				try {
					outs.println("<html><script type='text/javascript'>");
					outs.println("alert(\"" + msg + "\");");
					outs.println("</script></html>");
					outs.flush();
				} finally {
					outs.close();
				}
			}
				
		}
		return null;
	}
	
	/**
	 * 
	 * Method Name : uploadImg <BR/>
	 * Description : CK에디터 이미지 파일 업로드 <BR/>
	 *
	 * History <BR/>
	 * 2023. 01. 30. 이한상 최초작성 <BR/>
	 * 2023. 02. 13. 이한상 수정 / 이미지 업로드용 jsp를 사용하지 않도록 수정 <BR/>
	 *
	 * @param request
	 * @param response
	 * @param requestData
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/uploadImg.do")
	public void uploadImg(HttpServletRequest request,HttpServletResponse response, DataRequest dataRequest) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
    	Map<String, UploadFile[]> uploadFiles = dataRequest.getUploadFiles();
    	
    	String strBoardStorePath = AppProperties.getProperty("cmnNoticeBoard.fileStorePath");
    	
		if(uploadFiles != null && uploadFiles.size() > 0) {
			Set<Entry<String, UploadFile[]>> entries = uploadFiles.entrySet();
			for(Entry<String, UploadFile[]> entry : entries) {
				UploadFile[] uFiles = entry.getValue();
				for(UploadFile uFile : uFiles){
					File file = uFile.getFile();
					String fileName = uFile.getFileName();				//파일명
			        //파일을 바이트 배열로 변환
					String strTempPath = file.getPath();	
			        //이미지를 업로드할 디렉토리를 정해준다
//			        String uploadPath = serverEnv.getGlobalFileStorePath()  +   strBoardStorePath;
			        String uploadPath = AppProperties.getProperty("Globals.fileStorePath")  +   strBoardStorePath;
			        String strSaveNameFileNm = FileUtil.getEncryptFileNm();
			        strSaveNameFileNm = FileUtil.uploadFile(uploadPath, strTempPath, strSaveNameFileNm, true);
			        String fileUrl= request.getContextPath()+"/CmnFile/imgFreeBoard.do?fileNm="+strSaveNameFileNm;
			        map.put("uploaded", 1);
			        map.put("filename", fileName);
			        map.put("url", fileUrl);
			        
				}
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		try{
			mapper.writeValue(response.getWriter(), map);
		}catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@RequestMapping("/imgFreeBoard.do")
	public void imgFreeBoard(HttpServletRequest request,HttpServletResponse response, DataRequest dataRequest) throws IOException {
		String fileNm = request.getParameter("fileNm");
		String strGlobalsFilePath = AppProperties.getProperty("Globals.fileStorePath");
		String strBoardStorePath = AppProperties.getProperty("cmnNoticeBoard.fileStorePath");
		
		String imagePath = strGlobalsFilePath +  strBoardStorePath + File.separator+ fileNm;
		
		java.io.File file = new java.io.File(imagePath);
		byte b[] = new byte[(int) file.length()];
		
		if (file.isFile()) {
			BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
			BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
		
			int read = 0;
			while ((read = fin.read(b)) != -1) {
				outs.write(b, 0, read);
			}
			outs.close();
			fin.close();
		}
	}
}
