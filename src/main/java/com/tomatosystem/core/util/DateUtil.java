package com.tomatosystem.core.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
	/**
	 * 날짜 Java 객체를 날짜 문자열로 변환하여 반환한다. 
	 * @param date Java util 패키지 및 sql.Time 객체 등
	 * @return
	 */
	public static String dateToString(Object date){
		if (date instanceof String)
			return ((String) date);
		if (date instanceof java.sql.Date)
			return dateToString(((java.sql.Date) date).getTime());
		if (date instanceof java.util.Date)
			return dateToString(((java.util.Date) date).getTime());
		if (date instanceof Time)
			return dateToString(((Time) date).getTime());
		if (date instanceof Timestamp)
			return dateToString(((Timestamp) date).getTime());
		if (date instanceof Calendar) {
			return dateToString(((Calendar) date).getTimeInMillis());
		}
		return "00000000000000";
	}
	
	/**
	 * 해당하는 날짜포맷 문자열이 Date 유형인지 체크한다.  
	 * @param dateStr 날짜 포맷 문자열(ex- 20170101, 20170101101030)
	 * @return
	 */
	public static boolean isDate(String dateStr) {
		return isDate(dateStr, "yyyyMMdd");
	}
	
	/**
	 * 해당하는 날짜포맷 문자열이 Date 유형인지 체크한다.  
	 * @param dateStr 날짜 포맷 문자열(ex- 20170101, 20170101101030)
	 * @param strFormat 날짜 포맷 (ex- yyyyMMdd, yyyyMMddHHmmss)
	 * @return
	 */
	public static boolean isDate(String strDateStr, String strFormat) {
		
		String dateStr = strDateStr;
		dateStr = dateStr.replaceAll("[-.]", "");
		
		SimpleDateFormat sdf = new SimpleDateFormat(strFormat, Locale.getDefault());
		sdf.setLenient(false);
		try {
			sdf.parse(dateStr);
		} catch (ParseException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * yyyyMMdd 혹은 yyyy-MM-dd 형식의 날짜 문자열을 입력 받아 일(day)를  
     * 증감한다. day는 가감할 수를 의미하며, 음수를 입력할 경우 감한다.
	 * @param dateStr
	 * @param day
	 * @return
	 */
	public static String addDay(String dateStr, int day) {
        return addYearMonthDay(dateStr, 0, 0, day);
    }
	
	/**
	 * yyyyMMdd 혹은 yyyy-MM-dd 형식의 날짜 문자열을 입력 받아 월을 
     * 증감한다. month는 가감할 수를 의미하며, 음수를 입력할 경우 감한다.
	 * @param dateStr
	 * @param month
	 * @return
	 */
	public static String addMonth(String dateStr, int month) {
        return addYearMonthDay(dateStr, 0, month, 0);
    }
	
	/**
	 * 두 날짜를 비교한다.
	 * 0: 동일, 1: 두번째 날짜가 더 큼, -1: 첫번째 날짜가 더큼, -99: 날짜 비교 오류시
	 * @param sourceDate 비교할 첫번째 날짜 (ex-20170101)
	 * @param targetDate 비교할 두번째 날짜 (ex-20170101)
	 * @return
	 */
	public static int compare(String sourceDate, String targetDate) {
		
		return compare(sourceDate, targetDate, "yyyyMMdd");
	}
	
	/**
	 * 두 날짜를 비교한다.
	 * 0: 동일, 1: 두번째 날짜가 더 큼, -1: 첫번째 날짜가 더큼, -99: 날짜 비교 오류시
	 * @param sourceDate 비교할 첫번째 날짜
	 * @param targetDate 비교할 두번째 날짜
	 * @param strFormat 날짜 포맷(ex - yyyyMMdd, yyyy-MM-dd 등등)
	 * @return
	 */
	public static int compare(String sourceDate, String targetDate, String strFormat) {
		
		SimpleDateFormat sdf = new SimpleDateFormat(strFormat, Locale.getDefault());
		try {
			Date srcDate = sdf.parse(sourceDate);
			Date tgtDate = sdf.parse(targetDate);
			
			long srcTime = srcDate.getTime();
			long tgtTime = tgtDate.getTime();
			
			if(srcTime ==  tgtTime) return 0;
			else if(srcTime >  tgtTime) return -1;
			else if(srcTime <  tgtTime) return 1;
		} catch (ParseException e) {
			return -99;
		}
		
		return -99;
	}
	
	/**
     * 날짜형태의 String의 날짜 포맷을 변경한다.
     *
     * @param  strSource       바꿀 날짜 String
     * @param  fromDateFormat  기존의 날짜 형태
     * @param  toDateFormat    원하는 날짜 형태
     * @return  소스 String의 날짜 포맷을 변경한 String
     */
    public static String format(String strSource, String fromDateFormat, String toDateFormat) {
        if(strSource == null || "".equals(strSource)) return "";
    	
    	SimpleDateFormat sdfFrom = null;
        SimpleDateFormat sdfTo = null;
        Date date = null;
        try {
        	sdfFrom = new SimpleDateFormat(fromDateFormat, Locale.getDefault());
        	sdfTo = new SimpleDateFormat(toDateFormat, Locale.getDefault());
        	
        	date = sdfFrom.parse(strSource);
        	return sdfTo.format(date);
        }catch(ParseException e) {
        	return "";
        }catch(Exception e) {
        	return "";
        }
    }   
	
    /**
     * 해당 월의 마지막일자를 반환한다. 
     * @param sDate
     * @return
     */
    public static String getMonthLastDay(String dateStr) {
    	Calendar cal = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    	
    	Date tdate = null;
    	try {
			tdate = sdf.parse(dateStr);
		} catch (ParseException e) {
			return "";
		}
    	cal.setTime(tdate);
    	
    	int endDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    	String endOfMonth = String.valueOf(endDay);
    	
    	return dateStr.substring(0,6) + endOfMonth;
    }
    
    private static String addYearMonthDay(String sDate, int year, int month, int day) {
    	boolean isDate = isDate(sDate);
    	
    	if(isDate){
    		Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            try {
                cal.setTime(sdf.parse(sDate));
            } catch (ParseException e) {
                return "";
            }
            
            if (year != 0) 
                cal.add(Calendar.YEAR, year);
            if (month != 0) 
                cal.add(Calendar.MONTH, month);
            if (day != 0) 
                cal.add(Calendar.DATE, day);
            
            return sdf.format(cal.getTime());
    	}else{
    		return "";
    	}
    }
    
    private static String dateToString(long time){
		Calendar calendar = Calendar.getInstance();
		if (time != 0L)
			calendar.setTimeInMillis(time);
		else
			calendar.setTime(new java.util.Date());

		StringBuffer sb = new StringBuffer();
		sb.append(calendar.get(1));
		sb.append(doubleDigitMonth(calendar.get(2)));
		sb.append(doubleDigit(calendar.get(5)));

		sb.append(doubleDigit(calendar.get(11)));
		sb.append(doubleDigit(calendar.get(12)));
		sb.append(doubleDigit(calendar.get(13)));

		return sb.toString();
	}
	
	private static String doubleDigitMonth(int month) {
		int m = month;
		++m;
		return doubleDigit(m);
	}

	private static String doubleDigit(int number) {
		if (number < 10) {
			return "0"+String.valueOf(number);
		} else {
			return String.valueOf(number);
		}
	}
}