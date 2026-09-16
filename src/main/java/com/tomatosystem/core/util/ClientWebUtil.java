package com.tomatosystem.core.util;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class ClientWebUtil {
	/**
	 * 접속한 유저의 리모트 아이피를 가지고 옵니다.
	 * @param request
	 * @return
	 */
	public static String getAccessIp( HttpServletRequest request )
	{
		String ip = request.getHeader("X-Forwarded-For");
		
		try
		{
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getHeader("Proxy-Client-IP");  
			}  

			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getHeader("WL-Proxy-Client-IP");  
			}  

			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getHeader("HTTP_CLIENT_IP");  
			}  

			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
			}  

			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			    ip = request.getRemoteAddr();  
			}
			
			
			//2023.08.11 최동원 추가--------------
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
                ip = request.getHeader("X-Real_IP");  
            }  

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
                ip = request.getHeader("X-RealIp");  
            }  

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
                ip = request.getHeader("REMOTE_ADDR");  
            }  
			
			if(ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
			    InetAddress address = InetAddress.getLocalHost();
			    //ip = address.getHostName() + "/" + address.getHostAddress();
			    ip = address.getHostAddress();
			}
			//2023.08.11 최동원 추가--------------
		}
		catch (Exception ex) 
		{
			ip = "unknown ip address";
		}
		return ip;
	}
	
	/**
	 * 
	 * <pre>
	 * 메소드명	: getBrowserInfoFromRequest
	 * 설	 명	: user-agent로 부터 Browser 정보를 찾아서 리턴
	 * </pre>
	 *
	 * @param request
	 * @return
	 */
	public static String getBrowserInfo(HttpServletRequest request) {
		String strUserAgent = request.getHeader("user-agent");
		String strBrsInfo = "N/A";
		
		// 필요할 경우 추가하되 순서에 유의(각 브라우저 별 user-agent 형태를 참조)
		// 예) Chrome의 경우 user-agent가 아래와 같은 형식이기 때문에 Safari보다 먼저 검색되어져야 정확히 판별할 수 있음
		// Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36
		String[] listBrowserType = {"Edge", "Trident", "MSIE", "Whale", "Chrome", "Firefox", "Safari", "Opera"};
		for(String type : listBrowserType) {
			if(strUserAgent.contains(type)) {
				// 해당 브라우저부터 시작하는 서브 스트링을 가져온 다음
				// 다음문자열이 /인지 공백인지 확인
				int iIdxType = strUserAgent.indexOf(type);
				String strBrsInfoStartFromBrowserType = strUserAgent.substring(iIdxType);
				
				int iPosNextChar = type.length();
				char charNext = strBrsInfoStartFromBrowserType.charAt(iPosNextChar);
				if(charNext == '/') { // /일 경우 ;이나 공백을 찾아서 그 뒤로 절삭하여 리턴
					int iIdxSemiColon = strBrsInfoStartFromBrowserType.indexOf(";");
					if(iIdxSemiColon == -1) { 
						int iIdxSpace = strBrsInfoStartFromBrowserType.indexOf(" ");
						if(iIdxSpace == -1) {
							strBrsInfo = strBrsInfoStartFromBrowserType; // ;도 없고 공백도 없는 경우 그냥 그대로 리턴
							break;
						} else {
							strBrsInfo = strBrsInfoStartFromBrowserType.substring(0, iIdxSpace);
							break;
						}
					} else {
						strBrsInfo = strBrsInfoStartFromBrowserType.substring(0, iIdxSemiColon);
						break;
					}
				}else if(charNext == ' ') { // 공백이면 공백으로 split해서 0번이랑 1번을 합친다
					String[] listBrsInfoStartFromBrowserType = strBrsInfoStartFromBrowserType.split(" ");
					if(listBrsInfoStartFromBrowserType.length == 1) { // 공백도 없는 경우 브라우저 타입 그대로 리턴
						strBrsInfo = strBrsInfoStartFromBrowserType; // ;도 없고 공백도 없는 경우 그대로 리턴
						break;
					} else {
						strBrsInfo = listBrsInfoStartFromBrowserType[0] + " " + listBrsInfoStartFromBrowserType[1];
						break;
					}
				} else { break; }
			}
		}
		
		// trim 및 괄호 제거 등 후처리
		strBrsInfo = strBrsInfo.replaceAll("\\[", "").replaceAll("\\]","")
							   .replaceAll("\\{", "").replaceAll("\\}","")
							   .replaceAll("\\(", "").replaceAll("\\)","");
		
		return strBrsInfo.trim();
	}
	
	/**
     * 
     * <pre>
     * 메소드명	: getOsTypeInfo
     * 설	 명		: 접속자(User-Agent)의 OS 정보 get	
     * </pre>
     *
     * @author	: Park. ju wan
     *
     * 이력사항
     * 2013. 3. 19. Park. ju wan 최초작성 
     *
     * @param request
     * @return
     */
    public static String getOsTypeInfo(HttpServletRequest request) {
        String strOsTypeInfo = "";
        String user_agent = request.getHeader("User-Agent");
        //현재까지는 Mac과 Window와 Linux 만 고려대상이다.
        Pattern pattern = Pattern.compile("((Mac|Windows|Linux) ([[A-Za-z0-9_.-]*]?\\s?)*)[;$]");
        Matcher matcher = pattern.matcher(user_agent);
        
        while (matcher.find()) {
        	strOsTypeInfo = matcher.group(1);
            break;
        }
        
        if(strOsTypeInfo.indexOf("Windows") != -1)
        {
         if (strOsTypeInfo.indexOf("Windows 3.11") != -1 || strOsTypeInfo.indexOf("Win16") != -1)
       	  strOsTypeInfo = "Windows 3.11";
         else if (strOsTypeInfo.indexOf("Windows 95") != -1 || strOsTypeInfo.indexOf("Win95") != -1 || strOsTypeInfo.indexOf("Windows_95") != -1)
          strOsTypeInfo = "Windows 95";
         else if (strOsTypeInfo.indexOf("Windows 98") != -1 || strOsTypeInfo.indexOf("Win98") != -1)
          strOsTypeInfo = "Windows 98";
         else if (strOsTypeInfo.indexOf("Windows ME") != -1)
          strOsTypeInfo = "Windows ME";
         else if (strOsTypeInfo.indexOf("Windows NT 3.1") != -1)
          strOsTypeInfo = "Windows NT 3.1";
         else if (strOsTypeInfo.indexOf("Windows NT 3.5") != -1)
          strOsTypeInfo = "Windows NT 3.5";
         else if (strOsTypeInfo.indexOf("Windows NT 4.0") != -1 || strOsTypeInfo.indexOf("WinNT 4.0") != -1 || strOsTypeInfo.indexOf("WinNT") != -1)
          strOsTypeInfo = "Windows NT 4.0";
         else if(strOsTypeInfo.indexOf("Windows NT 5.0") != -1 || strOsTypeInfo.indexOf("Windows 2000") != -1)
          strOsTypeInfo = "Windows 2000";
         else if(strOsTypeInfo.indexOf("Windows NT 5.1") != -1 || strOsTypeInfo.indexOf("Windows XP") != -1)
          strOsTypeInfo = "Windows XP";
         else if(strOsTypeInfo.indexOf("Windows NT 5.2") != -1)
          strOsTypeInfo = "Windows Server 2003";
         else if(strOsTypeInfo.indexOf("Windows NT 6.0") != -1)
          strOsTypeInfo = "Windows Vista";
         else if(strOsTypeInfo.indexOf("Windows NT 6.1") != -1) 
          strOsTypeInfo = "Windows 7";
         else if(strOsTypeInfo.indexOf("Windows NT 6.2" ) != -1) 
             strOsTypeInfo = "Windows 8";
         else if(strOsTypeInfo.indexOf("Windows NT 10.0") != -1)
        	    strOsTypeInfo = "Windows 10 or 11";
        }
        else if (strOsTypeInfo.indexOf("Open BSD") != -1 || strOsTypeInfo.indexOf("OpenBSD") != -1)
          strOsTypeInfo = "Open BSD";
        else if (strOsTypeInfo.indexOf("Sun OS") != -1 || strOsTypeInfo.indexOf("SunOS") != -1)
         strOsTypeInfo = "Sun OS";
        else if (strOsTypeInfo.indexOf("Linux") != -1 || strOsTypeInfo.indexOf("X11") != -1)
         strOsTypeInfo = "Linux";
        else if (strOsTypeInfo.indexOf("Mac OS") != -1 || strOsTypeInfo.indexOf("Mac_PowerPC") != -1 || strOsTypeInfo.indexOf("Macintosh") != -1)
         strOsTypeInfo = "Mac OS";
        else if (strOsTypeInfo.indexOf("QNX") != -1)
         strOsTypeInfo = "QNX";
        else if (strOsTypeInfo.indexOf("BeOS") != -1)
         strOsTypeInfo = "BeOS";
        else if (strOsTypeInfo.indexOf("OS/2") != -1)
         strOsTypeInfo = "OS/2";
        
        return strOsTypeInfo;
    }
}
