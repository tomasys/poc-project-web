package com.tomatosystem.core.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.tomatosystem.core.exception.AppWorksException;

public class CalendarUtil {
	
	public static String YYYY_MM_DD_HH_MM_SS = "yyyyMMddHHmmss";

	public static String YYYY_MM_DD_HH_MM = "yyyyMMddHHmm";

	public static String YYYY_MM_DD_HH = "yyyyMMddHH";

	public static String YYYY_MM_DD = "yyyyMMdd";
	
	public static java.util.Date stringToDate(Object date) throws AppWorksException {
		if (date instanceof java.util.Date)
			return ((java.util.Date) date);
		if (date instanceof String) {
			return stringToDate((String) date);
		}
		return null;
	}
	public static java.util.Date stringToDate(String date) throws AppWorksException {
		return stringToDate(date, null);
	}

	public static java.util.Date stringToDate(String date, String format)
			throws AppWorksException {
		int length = date.length();

		SimpleDateFormat _format = null;
		try {
			if (length == 8)
				_format = new SimpleDateFormat(YYYY_MM_DD);
			else {
				_format = new SimpleDateFormat(
						YYYY_MM_DD_HH_MM_SS);
			}
			return _format.parse(date);
		} catch (ParseException e) {
			throw new AppWorksException(e.getMessage());
		}
	}

	public static String dateToString(Object date) {
		if (date instanceof String)
			return ((String) date);
		if (date instanceof java.sql.Date)
			return dateToString((java.sql.Date) date);
		if (date instanceof java.util.Date)
			return dateToString((java.util.Date) date);
		if (date instanceof Time)
			return dateToString((Time) date);
		if (date instanceof Timestamp)
			return dateToString((Timestamp) date);
		if (date instanceof Calendar) {
			return dateToString((Calendar) date);
		}
		return "00000000000000";
	}

	public static String dateToString(java.util.Date date) {
		return dateToString(date.getTime());
	}

	public static String dateToString(java.sql.Date date) {
		return dateToString(date.getTime());
	}

	public static String dateToString(Time time) {
		return dateToString(time.getTime());
	}

	public static String dateToString(Timestamp time) {
		return dateToStringt(time.getTime());
	}

	public static String dateToString(Calendar cal) {
		return dateToString(cal.getTimeInMillis());
	}

	public static String dateToStringt(long time) {
		Calendar calendar = Calendar.getInstance();

		if (time != 0L)
			calendar.setTimeInMillis(time);
		else
			calendar.setTime(new java.util.Date());

		StringBuffer sb = new StringBuffer();
		sb.append(calendar.get(1));
		sb.append(doubleDigitMonth(calendar.get(2)));
		sb.append(doubleDigit(calendar.get(5)));
		if (calendar.get(11) != 0) {
			sb.append(doubleDigit(calendar.get(11)));
			sb.append(doubleDigit(calendar.get(12)));
			sb.append(doubleDigit(calendar.get(13)));
		}

		return sb.toString();
	}

	public static String dateToString(long time) {
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

	public static String doubleDigitMonth(int month) {
		++month;
		return StringUtil.doubleDigit(month);
	}

	public static String doubleDigit(int day) {
		return StringUtil.doubleDigit(day);
	}
	
	
}