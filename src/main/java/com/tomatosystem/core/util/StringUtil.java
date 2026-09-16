package com.tomatosystem.core.util;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Clob;

public class StringUtil {
	/**
	 * 값이 Null인 경우 공백('') 문자열을 반환하고, Null이 아니면 해당 값을 반환한다.
	 * @param value
	 * @return
	 */
	public static String fixNull(Object value){
		if(value == null) return "";
		
		return value.toString().trim();
	}
	
	/**
	 * 문자열이 null 이나 "" 일경우 대체문자 리턴하고, 아닐경에는 파라미터로 넘어온 문자열을 그대로 리턴한다.
	 * @param value 문자열
	 * @param replace 대체문자
	 * @return String
	 */
	public static String fixNull(String value, String replace) {
		return value == null || "".equals(value) ? replace : value;
	}
	
    public static String fixNull(Object field, String replace) {
    	if(field == null) return replace;
    	else {
    		return field.toString();
    	}
    }
    
	/**
	 * 해당 문자열이 Null이거나 공백('')인지 여부를 반환한다.
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(Object value){
		return (value == null) ? true : ((String)value).length()==0;
	}
	
//	private static final String EMPTY_STRING = "";

	public static String nullToEmpty(Object o) {
		return o == null ? "" : (String) o;
	}

	public static boolean isInteger(char c) {
		return c >= 48 && c <= 57;
	}

	public static boolean isNotNull(Object o) {
		return o != null;
	}
	
	public static boolean isNullEmpty(Object o) {
		return o == null || fixNull(o).length() == 0;
	}

	public static boolean isNotNullEmpty(String s) {
		return s != null && s.length() > 0;
	}


	public static String doubleDigit(int number) {
		StringBuffer sb = new StringBuffer();
		if (number < 10) {
			sb.append('0');
			sb.append(number);
		} else {
			sb.append(number);
		}

		return sb.toString();
	}
	
	/**
	 * 문자열의 좌측(Left)을 특정 문자로 패딩한 값을 반환한다.
	 * @param str - 문자열
	 * @param size - 패딩 사이즈(Length)
	 * @param padChar - 패딩 문자
	 * @return
	 */
	public static String lPad(String psStr, int size, String padChar){
		String str = psStr;
		byte[] b = str.getBytes();
		int len = b.length;
		int tmp = size - len;
		
		for (int i=0; i < tmp ; i++){
			str = padChar + str;
		}
		return str;
	}
	
	/**
	 * 문자열의 우측(Right)을 특정 문자로 패딩한 값을 반환한다.
	 * @param str - 문자열
	 * @param size - 패딩 사이즈(Length)
	 * @param padChar - 패딩 문자
	 * @return
	 */
	public static String rPad(String psStr, int size, String padChar){
		String str = psStr;
		byte[] b = str.getBytes();
		int len = b.length;
		int tmp = size - len;
		
		for (int i=0; i < tmp ; i++){
			str += padChar;
		}
		return str;
	}
	
	/**
	 * 해당 인코딩의 문자열을 바이트 단위로 자른다.
	 * @param strTxt - 대상 문자열
	 * @param startIndex - 시작 인덱스
	 * @param endIndex - 끝 인덱스
	 * @param encoding - 문자열 인코딩
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String splitByteString(String strTxt, int startIndex, int endIndex, String encoding) throws UnsupportedEncodingException{
		StringBuffer buf = new StringBuffer();
		int len = 0;
		for(char ch : strTxt.toCharArray()){
			if(len >= startIndex && len<endIndex){
				buf.append(ch);
			}
			
			len += String.valueOf(ch).getBytes(encoding).length;
		}
		
		return buf.toString();
	}
	
	public static String getString(Object str) {
		return getString(str, "");
	}
	
	public static String getString(Object str, String defaultStr) {
		if (str == null) {
			return defaultStr;
		} else {
			String value = null;
			if (str instanceof String) {
				value = (String) str;
			} else if (str instanceof Clob) {
				value = clob2String((Clob) str);
			} else {
				value = String.valueOf(str);
			}

			return isBlank(value) ? defaultStr : value;
		}
	}

	public static String clob2String(Clob clob) {
		try {
			StringBuffer e = new StringBuffer();
			Reader reader = clob.getCharacterStream();
			char[] buff = new char[1024];
			boolean nchars = false;

			int nchars1;
			while ((nchars1 = reader.read(buff)) > 0) {
				e.append(buff, 0, nchars1);
			}

			return e.toString();
		} catch (Exception arg4) {
			return "";
		}
	}
	
	/**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * @since 2.0
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * <pre>
     * 특정문자열을 변경하는 메소드
     * </pre>
     * @param strFind java.lang.String 문자열
     * @param strTo java.lang.String 변경할 문자열
     * @param strScource java.lang.String 실제 문자열
     * @see java.lang.StringBuffer
     * @see java.lang.StringBuffer#append(java.lang.String)
     * @see java.lang.StringBuffer#toString()
     * @return java.lang.String 변경된 문자열
     */
	public static String replaceString(String strFind, String strTo, String strScource) {
		StringBuffer strbfContent = new StringBuffer();
		
		while(strScource.length() > 0) {
			int intPosition = strScource.indexOf(strFind);
				
			if (intPosition == -1) {
				strbfContent.append(strScource);
				break;
			}
			
			if (intPosition != 0) strbfContent.append(strScource.substring(0, intPosition));

			strbfContent.append(strTo);	
			
			if (strScource.length() == intPosition + 1)  break;	

			strScource = strScource.substring(intPosition + strFind.length());
		}
		
		return strbfContent.toString();
	}
}
