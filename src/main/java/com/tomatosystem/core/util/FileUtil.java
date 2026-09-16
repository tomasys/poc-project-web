package com.tomatosystem.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

//import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
//import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
//import org.tmt.core.constants.Alert;
//import org.tmt.core.exception.AppWorksException;
//import org.tmt.core.resource.AppProperties;

/**
 * 
 * @author datazone
 *
 */
public class FileUtil {
	
	/**
	 * 하나의 파일을 업로드 한다	
	 * @param destFolder 파일 업로드 경로 폴더
	 * @param uploadTempFile 업로드 할 파일명
	 * @param fileName 업로드한 파일이 저장될 이름
	 * @param overWrite 기존에 파일이 있을경우 덮어씌울지 여부, false 인 경우 파일명에 Unique 한 숫자를 붙임
	 * @return 업로드된 파일명
	 * @throws IOException
	 */
	public static String uploadFile(String destFolder, String uploadTempFile, String fileName, boolean overWrite) throws IOException{
		File sourceFile = null;
		try {
			if(uploadTempFile == null || "".equals(uploadTempFile)) return null;
			
			File destFileDir = new File(destFolder);
			if(!destFileDir.exists()){
				destFileDir.mkdirs();
			}
			
			sourceFile = new File(uploadTempFile);
			String sourceFolder = sourceFile.getParent();
			String sourceFileName = sourceFile.getName();
			
			if(overWrite == false) {
				fileName = getSafeFileName(destFolder, fileName);
			}
			
			moveFile(sourceFolder, sourceFileName, destFolder, fileName);
			
			return fileName;
		} catch(Exception e) {
			throw new IOException(e.getMessage());
		} finally {
			if(sourceFile != null && sourceFile.exists()){
				sourceFile.delete();
			}
		}
	}
	
	/**
     * 기존 파일의 정보와 이동시킬 정보를 파라미터로 받아서 기존의 파일을 새로운 위치로 이동시킨다.
     * @param strUploadDirectory 파일이 위치한 Directory
     * @param strTargetFileName 이동시킬 파일명
     * @param strRenameFileDir 옮기고자하는 Directory
     * @param strRenameFileName 옮길 파일명
     * @return 이동된 파일의 절대경로
	 * @throws IOException 
     */
    public static String moveFile(String strUploadDirectory, String strTargetFileName, String strRenameFileDir, String strRenameFileName) throws IOException {
    	File file = new File(strUploadDirectory + File.separator + strTargetFileName);
    	if(strRenameFileName == null || "".equals(strRenameFileName.trim())) strRenameFileName = strTargetFileName;
    	
    	String movedPath = null;
    	if(file.exists()) {
    		File fileTo = new File(strRenameFileDir);
    		if(fileTo.exists() == false) {
    			fileTo.mkdirs();
    		}
    		File renameDes = new File(fileTo, strRenameFileName);
    		if(renameDes.exists()) renameDes.delete();
    		
    		copyFile(file, renameDes);
    		
    		movedPath = renameDes.getAbsolutePath();
    	}
    	
    	return movedPath;
    }
    
    /**
	 * 통합정보시스템 파일 Export 디렉토리 경로를 반환한다.
	 * @param request
	 * @return
	 * @throws AppException
	 */
	public static String getExportFileDir(HttpServletRequest request) {
		String strPath = request.getServletContext().getRealPath("/") + "export";
		
		//출력용 디렉토리가 존재하지 않으면, 자동생성해준다.
		File dir = new File(strPath);
		if(dir.exists() == false){
			dir.mkdirs();
		}
		
		return strPath;
	}
	
	/**
	 * 해당 디렉토리에 생성하고자 하는 파일의 존재 여부를 판별하여, 파일이 존재 할경우 파일명과 확장자 사이에 일련번호를 부여한 파일명을 생성해 리턴한다.
	 * @param fileDir 생성하고자 하는 파일이 위치할 디렉토리
	 * @param fileName 생성하고자 하는 파일명
	 * @return 변경된 파일명
	 * @throws Exception
	 */
	private static String getSafeFileName(String fileDir, String fileName) {
		String resultFileName = null;
		File toFile = new File(fileDir + File.separator + fileName);
		if(toFile.exists()) {
			File fileDirectory = new File(fileDir); //서버의 업로드 디렉토리 내에 일련번호가 부여된 패턴의 파일 리스트를 조회한다.
			int maxSeq = 0; //최대 일련번호

			FilenameCheckFilter filenameFilter = new FilenameCheckFilter(fileName); //일련번호가 부여된 파일명의 형식만 조회 하기 위한 Filter
			String[] matchFileList = fileDirectory.list(filenameFilter); //도일파일명을 리스트업한다.
			String matchFileName; //파일명을 조작하기위한 임시 파일명 인스턴스
			for(int i = 0, len = matchFileList.length; i < len; i++) { //파일리스트중 최대 파일 일련번호를 구한다.
				matchFileName = filenameFilter.getSeq(matchFileList[i]);
				if("".equals(matchFileName)) matchFileName = "(0)";
				matchFileName = matchFileName.substring(1, matchFileName.length() - 1); // Seq 양옆의 "(", ")"를 삭제한다.
				int seq = Integer.parseInt(matchFileName);
				if(maxSeq < seq) maxSeq = seq; //최대 일련번호를 구한다.
			}

			maxSeq++; //기존 최대 일련번호에서 1증가
			resultFileName = filenameFilter.getFileName() + "(" + maxSeq + ")" + ("".equals(filenameFilter.getFileExt()) ? "" : "." + filenameFilter.getFileExt()); //Sequence가 부여된 파일명을 생성한다.
		} else {
			resultFileName = fileName;
		}
		
		return resultFileName;
	}
		
	public static String getFileExtNm(String strFileName){
		if(strFileName.endsWith(".tmp")){
			strFileName = strFileName.indexOf(".") != -1 ? strFileName.substring(0, strFileName.length()-4) : strFileName;
		}
		int iFileExtIndex = strFileName.lastIndexOf(".");
		return iFileExtIndex > -1 ? strFileName.substring(iFileExtIndex+1).toLowerCase() : ""; 
	}
	
	public static void fileDownloadWrapper(String fileFullPath,
			HttpServletRequest request, HttpServletResponse response,
			String logicalName) throws Exception {
		
		File downloadFile = new File(fileFullPath);
		if (!downloadFile.exists()){
			throw new FileNotFoundException();
		}
		fileFullPath = fileFullPath.replaceAll("([\\\\]+)|([/]+)", "/");
		//2019.01.22 sulmoiho 웹취약점 보완
		//상위 폴더에 대한 접근 취약점 방지
		String fileName = fileFullPath.substring(fileFullPath.lastIndexOf("/") + 1);
		if ((logicalName != null) && (logicalName.length() > 0)){
			fileName = logicalName;
		}
		
		fileName = HttpWebUtil.getUrlEncodedFileName(request, fileName);
		
		String resCharset = request.getHeader("res-charset");
		if ((resCharset == null) || (resCharset.equalsIgnoreCase(""))) {
			resCharset = "UTF-8";
		}

		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try {
			response.setContentType("application/x-msdownload" + ";charset=" + resCharset);
			//웹취약점 보완
			//외부에서 입력한 파일명을 헤더에 추가하는 경우에 HTTP 응답분할 취약점이 발생할 수 있으므로, \r, \n 문자를 제거한다.
    		response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName.replaceAll("[\\r\\n]", "") + "\"");

			in = new BufferedInputStream(new FileInputStream(downloadFile));
			out = new BufferedOutputStream(response.getOutputStream());
			int data;
			while((data = in.read()) != -1){
				out.write(data);
			}
			out.flush();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			in.close();
			out.close();
		}
	}
	
	/**
	 * 여러개의 파일을 압축파일(.zip)로 묶어서 다운로드를 수행한다.
	 * @param request
	 * @param response
	 * @param sourceFileDir
	 * @param fileName
	 * @param fileList
	 * @throws Exception
	 */
	public static void downloadAsZip(HttpServletRequest request, HttpServletResponse response, 
            String sourceFileDir, String fileName, List<Map<String, Object>> fileList) throws Exception {
		if(fileList != null && fileList.size() > 0){
			String strDestDir = FileUtil.getExportFileDir(request);
			
			File zipFile = new File(strDestDir + File.separator + fileName);
			
			FileInputStream fin = null;
			FileOutputStream fout = null;
			ZipArchiveOutputStream zos = null;
			ZipArchiveEntry zen = null;
			try {
				fout = new FileOutputStream(zipFile);
				zos = new ZipArchiveOutputStream(fout);
				
				Map<String, Object> fileItem;
				File file = null;
				int fLength;
				byte [] buf = new byte[2048];
				for(int i=0, len=fileList.size(); i<len; i++){
					fileItem = fileList.get(i);
					
					file = new File(sourceFileDir + File.separator + fileItem.get("FILE_PATH") + File.separator + fileItem.get("SAVE_FILE_NM"));
					if(!file.exists()) continue;
					
					zen = new ZipArchiveEntry((String) fileItem.get("FILE_NM"));
					zos.putArchiveEntry(zen);
					fin = new FileInputStream(file);
					while ( (fLength = fin.read(buf, 0, buf.length)) >= 0 ){
						zos.write(buf, 0, fLength);
					}
					fin.close();
					zos.closeArchiveEntry();
				}
				zos.close();		
			} catch (Exception e) {
				//파일다운로드시 오류가 발생했습니다.\n파일이 존재하지 않거나 네트워크가 불안정합니다.\n관리자에게 문의바랍니다
			    throw new Exception();
			} finally {
				if(fin != null){
					fin.close();
				}
				if(zos != null){
					zos.close();
				}
				if(fout != null){
					fout.close();
				}
			}
		
			try {
				FileUtil.fileDownloadWrapper(strDestDir+File.separator+fileName, request, response, fileName);
			} catch (Exception e) {
				//파일다운로드시 오류가 발생했습니다.\n파일이 존재하지 않거나 네트워크가 불안정합니다.\n관리자에게 문의바랍니다
			    throw new Exception();
			} finally {
				if(zipFile != null && zipFile.exists()){
					zipFile.delete();
				}
			}
		}
	}
	
	public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
		copyFile(srcFile, new File(destDir, srcFile.getName()));
	}
	
	public static void copyFile(File srcFile, File destFile) throws IOException {
		FileInputStream input = FileUtil.openInputStream(srcFile);
		FileOutputStream output = FileUtil.openOutputStream(destFile);
		try {
			copy(input, output);
		} finally {
			close(input);
			close(output);
		}
	}

	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) throw new IOException("is a directory"); 
			if (!file.canRead()) throw new IOException("cannot be read"); 
		} else throw new FileNotFoundException("does not exist");
		
		return new FileInputStream(file);
	}
	
	public static FileOutputStream openOutputStream(File file) throws IOException {
		if (file.exists()) {
            if (file.isDirectory()) throw new IOException("is a directory");
            if (file.canWrite() == false) throw new IOException("cannot be written to");
        } else {
            File parent = file.getParentFile();
            if (parent != null && parent.exists() == false) {
                if (parent.mkdirs() == false) throw new IOException("could not be created");
            }
        }
		
        return new FileOutputStream(file);
	}
	
	public static void writeStringToFile(File file, String data) throws IOException {
		writeStringToFile(file,data, null);
	}

	public static void writeStringToFile(File file, String data, String encoding) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file);
			write(data, out, encoding);
		} finally {
			close(out);
		}
	}
	
	public static boolean deleteFile(String fileFullPath){
		File file = new File(fileFullPath);
		if(!file.exists()) return false;
		
		return file.delete();
	}
	
	private static void  write(String data, OutputStream out, String encoding) throws IOException {
		if (data != null) {
			if (encoding == null) {
				out.write(data.getBytes());
			} else {
				out.write(data.getBytes(encoding));
			}
		}
	}
	
	private static long copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		long count = 0;
		int n = 0;
		while((n = input.read(buffer)) != -1) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
	private static void close(InputStream input) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void close(OutputStream output) {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getEncryptFileNm(){
		return UtilUuidMgr.randomUUID().toString();
	}
	/**
	 * File목록중 기준 파일명에 일련번호가 부여된 파일만 조회한다.
	 * @author TomstoSystem
	 */
	private static class FilenameCheckFilter implements FilenameFilter {
		String fileName;
		String fileExt;

		FilenameCheckFilter(String fileName) {
			if(fileName.indexOf(".") > -1) {
				int extPos = fileName.lastIndexOf(".");
				this.fileExt = fileName.substring(extPos + 1);
				this.fileName = fileName.substring(0, extPos);
			} else {
				this.fileName = fileName;
				this.fileExt = "";
			}
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public String getFileExt() {
			return fileExt;
		}
		
		public boolean accept(File file, String name) {
			if(name == null) return false;
			String seqPattern = getSeq(name);
			if(seqPattern == null) return false;
			if("".equals(seqPattern)) return true;
			else return Pattern.matches("[(][1-9]?[0-9]+[)]", seqPattern);
		}
		
		public String getSeq(String name) {
			if(name.startsWith(fileName)) {
				String pattern = null;
				if("".equals(fileExt)) {
					pattern = name.substring(fileName.length());
				} else {
					if(name.endsWith(fileExt) == false) return null;
					pattern = name.substring(fileName.length(), name.lastIndexOf(fileExt) - 1);
				}
				return pattern;
			} else return null;
		}
		
	}
}
