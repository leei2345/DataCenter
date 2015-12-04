package com.jinba.utils;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期转化工具类     把日期字符串转化成date类型
 * @author renze
 *
 */
public class Convert {
	private static int timezone = 0;
	static {
		timezone = TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60;
	}
	private static final Pattern	DPTN[]	= {
		//mm-dd-yyyy hh:mm
		Pattern.compile("(\\d{1,2})[\\s\\-\\/](\\d{1,2})[\\s\\-\\/](20\\d{2})\\s{0,2}((\\d{1,2})[:\\s](\\d{1,2})[:\\s]?(\\d{1,2})?)?"),
		// yyyy-MM-dd hh:mm										
		Pattern.compile("((20)?\\d{2}) {0,2}[\\.\\-/年] {0,2}(\\d{1,2}) {0,2}[\\.\\-/月] {0,2}(\\d{1,2}) {0,2}[日 \\s]{0,2}((上午)|(下午))?\\s{0,2}((\\d{1,2})[:\\s](\\d{1,2})[:\\s]?(\\d{1,2})?)?"),	
		// MM-dd hh:mm
		Pattern.compile("(\\d{1,2})[\\.\\-\\s/月](\\d{1,2})[日\\s]{0,2}((上午)|(下午))?\\s{0,2}((\\d{1,2})[:\\s](\\d{1,2})[:\\s]?(\\d{1,2})?)?"),
		 // hh:mm
		Pattern.compile("([今前昨]天)?\\s{0,4}(\\d{1,2})[:\\s]{1,3}(\\d{1,2})[:\\s]?(\\d{1,2})?"),
		// [今前昨]天
		Pattern.compile("[今前昨]天"),
		// xxx前
		Pattern.compile("((\\d{1,2})|(半))\\s*个?([天秒小时分钟周月]{1,2})前"),
		// 1小时20分钟前  17小时20分钟前
		Pattern.compile("(\\d{1,2})小?时(\\d{1,2})分钟?前"),
		//20100411
		Pattern.compile("(20\\d{2})[01]?(\\d{2})[012]?(\\d{2})"),
		
	};

	public static Date parseDate(Object obj) {
		if (null == obj) return null;
		if (obj instanceof Date)  return (Date) obj;
		if (obj instanceof Number) return new Date(((Number) obj).longValue());
		String str = ("" + obj).trim();
		if (0 == str.length() || "null".equalsIgnoreCase(str)) {
			return null;
		}
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		Matcher mt;
		//mm-dd-yyyy hh:mm
		//(\d{1,2})[\s\-\/](\d{1,2})[\s\-\/](20\d{2})\s{1,2}((\d{1,2})[:\s](\d{1,2}))?
		mt = DPTN[0].matcher(str);
		if (mt.find()) {
			int date = Integer.parseInt(mt.group(2));
			if (date == 0 || date > 31) {
				return null;
			}
			int month = Integer.parseInt(mt.group(1));
			if (month <= 0) return null;
			if (month > 12) {
				if(date > 0 && date <= 12 && month < 32){
					int tmp = month;
					month = date;
					date = tmp;
				}
				else return null;
			}
			String sy = mt.group(3);
			final int year = Integer.parseInt(sy);
			if (year < 2000 || year > 2099) {
				return null;
			}
			// has hh:mm:ss
			String hms = mt.group(4);
			if(null == hms || 0 == hms.length()){
				c.set(year, month-1, date, timezone > 0 ? timezone : 0, 0 , 0);
				return c.getTime();
			}
			final int hour = Integer.parseInt(mt.group(5));
			if (hour >= 24) {
				return null;
			}
			final int min = Integer.parseInt(mt.group(6));
			if (min >= 60) {
				return null;
			}
			String ssec = mt.group(7);
			final int sec = (null == ssec || 0 == ssec.length())? 0 : Integer.parseInt(ssec);
			c.set(year, month-1, date, hour, min, sec);
			return c.getTime();
		}
		
		// yyyy-MM-dd hh:mm:ss	
		mt = DPTN[1].matcher(str);
		if (mt.find()) {
			String sy = mt.group(1);
			if (sy.length() == 2) {
				sy = "20" + sy;
			}
			final int year = Integer.parseInt(sy);
			if (year < 2000 || year > 2099) {
				return null;
			}
			final int month = Integer.parseInt(mt.group(3)) - 1;
			if (month < 0 || month > 11) {
				return null;
			}
			final int date = Integer.parseInt(mt.group(4));
			if (date > 31) {
				return null;
			}
			// has hh:mm:ss
			String ss = mt.group(8);
			if(null == ss || 0 == ss.length()){
				//c.set(year, month, date);
				c.set(year, month, date, timezone > 0 ? timezone : 0, 0 , 0);
				return c.getTime();
			}
			int hour = Integer.parseInt(mt.group(9));
			if (hour >= 24) {
				return null;
			}
			final int min = Integer.parseInt(mt.group(10));
			if (min >= 60) {
				return null;
			}
			String ssec = mt.group(11);
			final int sec = (null == ssec || 0 == ssec.length())? 0 : Integer.parseInt(ssec);
			
			if("下午".equals(mt.group(5)) && hour < 12){
				hour += 12;
			}
			c.set(year, month, date, hour, min, sec);
			return c.getTime();
		}
		
		//  MM-dd hh:mm:ss
		mt = DPTN[2].matcher(str);
		if (mt.find()) {
			int year = c.get(Calendar.YEAR);
			final int month = Integer.parseInt(mt.group(1)) - 1;
			if (month < 0) {
				return null;
			}
			if (month > c.get(Calendar.MONTH)) {
				year--;
			}
			final int date = Integer.parseInt(mt.group(2));
			if (date > 31) {
				return null;
			}
			String p = mt.group(6);
			if(null == p || 0 == p.length()) {
				//c.set(year, month, date);
				c.set(year, month, date, timezone > 0 ? timezone : 0, 0, 0);
				return c.getTime();
			}
			
			int hour = Integer.parseInt(mt.group(7));
			if (hour >= 24) {
				return null;
			}
			final int min = Integer.parseInt(mt.group(8));
			if (min >= 60) {
				return null;
			}
			String ssec = mt.group(9);
			final int sec = (null == ssec || 0 == ssec.length())? 0 : Integer.parseInt(ssec);
			if("下午".equals(mt.group(3)) && hour < 12){
				hour += 12;
			}
			c.set(year, month, date, hour, min, sec);
			return c.getTime();
		}
		//  hh:mm
		mt = DPTN[3].matcher(str);
		if (mt.find()) {
			final int hour = Integer.parseInt(mt.group(2));
			if (hour >= 24) {
				return null;
			}
			final int min = Integer.parseInt(mt.group(3));
			if (min >= 60) {
				return null;
			}
			final String day = mt.group(1);
			if ("昨天".equals(day)) {
				c.add(Calendar.DAY_OF_MONTH, -1);
			} else if ("前天".equals(day)) {
				c.add(Calendar.DAY_OF_MONTH, -2);
			}
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, min);
			return c.getTime();
		}
		//// [今前昨]天
		mt = DPTN[4].matcher(str);
		if (mt.find()) {
			final String day = mt.group(0);
			if ("昨天".equals(day)) {
				c.add(Calendar.DAY_OF_MONTH, -1);
			} else if ("前天".equals(day)) {
				c.add(Calendar.DAY_OF_MONTH, -2);
			}
			return c.getTime();
		}
		// xxx前 "((\\d{1,2})|半)\\s*([天秒小时分钟周月]{1,2})前"
		mt = DPTN[5].matcher(str);
		if (mt.find()) {
			final String s = mt.group(4);
			long t;
			if ("月".equals(s)){
				t = 86400000 * 30;
			} else if ("周".equals(s)){
				t = 86400000 * 7;
			} else if ("天".equals(s)){
				t = 86400000;
			} else if("小时".equals(s)) {
				t = 3600000;
			}else if("时".equals(s)) {
				t = 3600000;
			} else if ("分钟".equals(s)) {
				t = 60000;
			} else if ("分".equals(s)) {
				t = 60000;
			}else if ("秒".equals(s)){
				t = 1000;
			} else {
				return null;
			}
			String vs = mt.group(1);
			if("半".equals(vs)){
				t = System.currentTimeMillis() - t / 2 ;
			} else {
				t = System.currentTimeMillis() - Integer.parseInt(vs) * t ;
			}
			return new Date(t);
		}
		// 1小时20分钟前
		//"(\\d{1,2})小时(\\d{1,2})分钟前"
		mt = DPTN[6].matcher(str);
		if (mt.find()) {
			final int hh= Integer.parseInt(mt.group(1));
			final int nn = Integer.parseInt(mt.group(2));
			final long t = 3600000 * hh + 60000 * nn;
			return new Date(System.currentTimeMillis() - t);
		}
		/**
		 * 添加新日期格式
		 */
		// yyyyMMdd
		mt = DPTN[7].matcher(str);
		if (mt.find()) {
			String sy = mt.group(1);
			final int year = Integer.parseInt(sy);
			if (year < 2000 || year > 2099) {
				return null;
			}
			final int month = Integer.parseInt(mt.group(2)) - 1;
			if (month < 0 || month > 11) {
				return null;
			}
			final int date = Integer.parseInt(mt.group(3));
			if (date > 31) {
				return null;
			}
			
			//c.set(year, month, date);
			c.set(year, month, date, timezone > 0 ? timezone : 0, 0, 0);
			return c.getTime();
		}
		return null;
	}

	public static void main(String[] args){
		System.out.println("timezone=" + timezone);
		String[] testdata = new String[]{
			"11-13 15:24",	
			"2009-8-30 16:42:10",
			"8-23 15:24",
			"2周前",
			"3  天前",
			"12  分钟前",
			"3天前",
			"前天  09:36",
			"昨天 09:21 ",
			"2010-12-17 00:23 ",
			"2010-12-17 ",
			"昨天 12:37 ",
			"2011-8-15 08:42",
			"25-7-2011 11:43:57",
			"1-9-2011",
			"06-03",
			"半小时前",
			"今天发表",
			"昨天发表",
			"前天发表",
			"06-03-2010",
			"02-01-2010 00:39",
			"3小时26分钟前",
			"2010-8-24 上午 01:17:32",
			"2010-8-24 下午 01:17:32",
			"7小时前   »",
			"4/29/2010 1:31:00",
			"2012 年 1 月 31 日",
			"17时20分前"
		};
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
		for(String s : testdata){
			Date d = parseDate(s);
			System.out.println(s + "\t\t" +  ((null==d)?d:df.format(d)) );
		}
		
	}

}
