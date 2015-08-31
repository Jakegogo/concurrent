package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日期工具类 <br/>
 * 修复多线程共用DateFormat的产生多线程问题,单线程只创建一个DateFormat
 * @author JY253
 */
public class DateUtils {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
	
	
	private static ThreadLocal<Map<String, DateFormat>> threadLocal = new ThreadLocal<Map<String, DateFormat>>() {
		protected synchronized Map<String, DateFormat> initialValue() {
			Map<String, DateFormat> formatorMap = new HashMap<String, DateFormat>(
					4);
			formatorMap.put(DATE_FORMAT, new SimpleDateFormat(DATE_FORMAT));
			formatorMap.put(DATE_FORMAT_YYYY_MM_DD, new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD));
			return formatorMap;
		}
	};


	/**
	 * 获取日期转换器
	 * @return
	 */
	public static DateFormat getDateFormat() {
		Map<String, DateFormat> map = threadLocal
				.get();
		return map.get(DATE_FORMAT);
	}

	/**
	 * 获取日期转换器
	 * @param format 格式
	 * @return
	 */
	public static DateFormat getDateFormat(String format) {
		Map<String, DateFormat> map = threadLocal
				.get();
		DateFormat dateFormat = map.get(format);
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat(format);
			map.put(format, dateFormat);
		}
		return dateFormat;
	}

	/**
	 * 解析字符串日期 默认格式yyyy-MM-dd HH:mm:ss
	 * 
	 * @param textDate
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String textDate) throws ParseException {
		return getDateFormat().parse(textDate);
	}

	/**
	 * 解析字符串日期
	 * 
	 * @param textDate
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String textDate, String format)
			throws ParseException {
		return getDateFormat(format).parse(textDate);
	}

	/**
	 * 格式化日志 默认格式yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String format(Date date) {
		return getDateFormat().format(date);
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param format
	 *            如yyyy-MM-dd
	 * @return
	 */
	public static String format(Date date, String format) {
		return getDateFormat(format).format(date);
	}

	/**
	 * 获取某天的开始
	 * 
	 * @param date
	 * @return
	 */
	public static Date getStartDatePointer(Date date) {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setTime(date);

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取某天的最后时间点
	 * 
	 * @param date
	 * @return
	 */
	public static Date getEndDatePointer(Date date) {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setTime(date);

		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.set(Calendar.MILLISECOND, 999);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取昨天的结束时间点
	 * 
	 * @return
	 */
	public static Date getLastDateEndPointer() {
		return getYesterdayEndPointer();
	}

	/**
	 * 获取今天的开始时间点
	 * 
	 * @return
	 */
	public static Date getTodayStartPointer() {
		Calendar currentDate = new GregorianCalendar();

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取今天的最后时间点
	 * 
	 * @return
	 */
	public static Date getTodayEndPointer() {
		Calendar currentDate = new GregorianCalendar();

		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.set(Calendar.MILLISECOND, 999);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取昨天的开始时间点
	 * 
	 * @return
	 */
	public static Date getYesterdayStartPointer() {
		Calendar currentDate = new GregorianCalendar();

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.add(Calendar.DATE, -1);
		currentDate.set(Calendar.MILLISECOND, 0);
		return (Date) currentDate.getTime().clone();
	}
	
	
	/**
	 * 获取date的昨天的开始时间点
	 * @param date
	 * @return
	 */
	public static Date getYesterdayStartPointer(Date date) {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setTime(date);

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.add(Calendar.DATE, -1);
		currentDate.set(Calendar.MILLISECOND, 0);
		return (Date) currentDate.getTime().clone();
	}
	

	/**
	 * 获取昨天的结束时间点
	 * 
	 * @return
	 */
	public static Date getYesterdayEndPointer(Date date) {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setTime(date);

		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.add(Calendar.DATE, -1);
		currentDate.set(Calendar.MILLISECOND, 999);
		return (Date) currentDate.getTime().clone();
	}
	
	/**
	 * 获取前天的开始时间点
	 * @return
	 */
	public static Date getAfterDayStartPointer(){
		Calendar currentDate = new GregorianCalendar();

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.add(Calendar.DATE, -2);
		currentDate.set(Calendar.MILLISECOND, 0);
		return (Date) currentDate.getTime().clone();
	}
	
	
	/**
	 * 获取date的前天的开始时间点
	 * @return
	 */
	public static Date getAfterDayStartPointer(Date date){
		Calendar currentDate = new GregorianCalendar();
		currentDate.setTime(date);

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.add(Calendar.DATE, -2);
		currentDate.set(Calendar.MILLISECOND, 0);
		return (Date) currentDate.getTime().clone();
	}
	

	/**
	 * 获取昨天的结束时间点
	 * 
	 * @return
	 */
	public static Date getYesterdayEndPointer() {
		Calendar currentDate = new GregorianCalendar();

		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.add(Calendar.DATE, -1);
		currentDate.set(Calendar.MILLISECOND, 999);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取本周的开始时间点
	 * 
	 * @return
	 */
	public static Date getWeekStartDatePointer() {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setFirstDayOfWeek(Calendar.MONDAY);

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		currentDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取本周的结束时间点
	 * 
	 * @return
	 */
	public static Date getWeekEndDatePointer() {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setFirstDayOfWeek(Calendar.MONDAY);
		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.set(Calendar.MILLISECOND, 999);
		currentDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取上周的开始时间点
	 * 
	 * @return
	 */
	public static Date getLastWeekStartPointer() {
		int mondayPlus = getMondayPlus();

		Calendar currentDate = new GregorianCalendar();
		currentDate.setFirstDayOfWeek(Calendar.MONDAY);

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		currentDate.add(Calendar.DATE, mondayPlus + 7 * -1);
		currentDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取上周的结束时间点
	 * 
	 * @return
	 */
	public static Date getLastWeekEndDatePointer() {
		int mondayPlus = getMondayPlus();
		Calendar currentDate = new GregorianCalendar();
		currentDate.setFirstDayOfWeek(Calendar.MONDAY);

		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.set(Calendar.MILLISECOND, 999);
		currentDate.add(Calendar.DATE, mondayPlus - 1);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取本月的开始时间点
	 * 
	 * @return
	 */
	public static Date getMonthStartPointer() {
		Calendar currentDate = new GregorianCalendar();

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		currentDate.set(Calendar.DATE, 1);
		return (Date) currentDate.getTime().clone();
	}
	
	/**
	 * 获取上一个月的开始时间点
	 * @return
	 */
	public static Date getLastMonthStartPointer(){
		Calendar currentDate = new GregorianCalendar();

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		currentDate.set(Calendar.DATE, 1);
		currentDate.add(Calendar.MONTH, -1);
		return (Date) currentDate.getTime().clone();
	}
	
	
	/**
	 * 获取date的上一个月的开始时间点
	 * @return
	 */
	public static Date getLastMonthStartPointer(Date date){
		Calendar currentDate = new GregorianCalendar();
		currentDate.setTime(date);

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		currentDate.set(Calendar.DATE, 1);
		currentDate.add(Calendar.MONTH, -1);
		return (Date) currentDate.getTime().clone();
	}
	
	
	/**
	 * 获取上月的结束时间点
	 * 
	 * @return
	 */
	public static Date getLastMonthEndPointer() {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setFirstDayOfWeek(Calendar.MONDAY);

		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.set(Calendar.MILLISECOND, 999);

		currentDate.set(Calendar.DATE, 1);
		currentDate.add(Calendar.DATE, -1);

		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取日期所在月的开始时间点
	 * 
	 * @param date
	 * @return
	 */
	public static Date getMonthStartPointer(Date date) {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setTime(date);

		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		currentDate.set(Calendar.DATE, 1);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获取本月的结束时间点
	 * 
	 * @return
	 */
	public static Date getMonthEndPointer() {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setFirstDayOfWeek(Calendar.MONDAY);

		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.set(Calendar.MILLISECOND, 999);

		currentDate.set(Calendar.DATE, 1);
		currentDate.add(Calendar.MONTH, 1);
		currentDate.add(Calendar.DATE, -1);

		return (Date) currentDate.getTime().clone();
	}

	/**
	 * 获得日期所在月的结束时间点
	 * 
	 * @param date
	 * @return
	 */
	public static Date getMonthEndPointer(Date date) {
		Calendar currentDate = new GregorianCalendar();
		currentDate.setTime(date);

		currentDate.setFirstDayOfWeek(Calendar.MONDAY);

		currentDate.set(Calendar.HOUR_OF_DAY, 23);
		currentDate.set(Calendar.MINUTE, 59);
		currentDate.set(Calendar.SECOND, 59);
		currentDate.set(Calendar.MILLISECOND, 999);

		currentDate.set(Calendar.DATE, 1);
		currentDate.add(Calendar.MONTH, 1);
		currentDate.add(Calendar.DATE, -1);

		return (Date) currentDate.getTime().clone();
	}


	/**
	 * 获取星期一是第几天
	 * @return
	 */
	private static int getMondayPlus() {
		Calendar cd = Calendar.getInstance();
		int dayOfWeek = cd.get(7) - 1;
		if (dayOfWeek == 1) {
			return 0;
		}
		return (1 - dayOfWeek);
	}

	/**
	 * 计算两天之间相差几天
	 * 
	 * @param startTime
	 * @param endTime
	 * @return 取得两个时间段的时间间隔 return t2 与t1的间隔天数 throws ParseException
	 *         如果输入的日期格式不是0000-00-00 格式抛出异常
	 */
	public static int betweenTwoDays(String startTime, String endTime) {
		String startYearStr = startTime.substring(0, 4);
		String endYearStr = endTime.substring(0, 4);
		String startMonthStr = startTime.substring(4, 6);
		String endMonthStr = endTime.substring(4, 6);
		String startDayStr = startTime.substring(6, 8);
		String endDayStr = endTime.substring(6, 8);
		String t1 = startYearStr + "-" + startMonthStr + "-" + startDayStr;
		String t2 = endYearStr + "-" + endMonthStr + "-" + endDayStr;
		int betweenDays = 0;
		Date d1;
		Date d2;
		try {
			d1 = parse(t1, DATE_FORMAT_YYYY_MM_DD);
			d2 = parse(t2, DATE_FORMAT_YYYY_MM_DD);
			Calendar c1 = Calendar.getInstance();
			Calendar c2 = Calendar.getInstance();
			c1.setTime(d1);
			c2.setTime(d2);
			int betweenYears = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
			betweenDays = c2.get(Calendar.DAY_OF_YEAR)
					- c1.get(Calendar.DAY_OF_YEAR);
			for (int i = 0; i < betweenYears; i++) {
				c1.set(Calendar.YEAR, (c1.get(Calendar.YEAR) + 1));
				betweenDays += c1.getMaximum(Calendar.DAY_OF_YEAR);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return betweenDays;
	}
	
	
	/**
	 * 计算日期相隔天数
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int betweenTwoDays(Date d1, Date d2) {
		int betweenDays;
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);
		int betweenYears = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
		betweenDays = c2.get(Calendar.DAY_OF_YEAR)
				- c1.get(Calendar.DAY_OF_YEAR);
		for (int i = 0; i < betweenYears; i++) {
			c1.set(Calendar.YEAR, (c1.get(Calendar.YEAR) + 1));
			betweenDays += c1.getMaximum(Calendar.DAY_OF_YEAR);
		}
		return betweenDays;
	}
	

	/***
	 * 判断是否为闰年
	 * 
	 * @param yearStr
	 * @return
	 */
	public static boolean isLoop(String yearStr) {
		int year = Integer.parseInt(yearStr);
		boolean flag = false;
		if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
			flag = true;
		}
		return flag;
	}
	

	public static boolean isLoop(int year) {
		boolean flag = false;
		if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
			flag = true;
		}
		return flag;
	}

	/****
	 * 计算出day的before天之前是哪一天
	 * 
	 * @param day
	 * @return
	 */
	public static String getDayBefore(String day, int beforeDay) {
		String resultDay;
		String YearStr = day.substring(0, 4);
		String MonthStr = day.substring(4, 6);
		String DayStr = day.substring(6, 8);

		int yearInt = Integer.parseInt(YearStr);
		int monthInt = Integer.parseInt(MonthStr);
		int dayInt = Integer.parseInt(DayStr);

		int[] month;
		if (isLoop(yearInt)) {
			month = new int[] { 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30,
					31 };
		} else {
			month = new int[] { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
					31 };
		}
		for (int i = 0; i < beforeDay; i++) {
			dayInt--;
			if (dayInt <= 0) {
				monthInt--;
				if (monthInt <= 0) {
					yearInt--;
					monthInt = 12;
					dayInt = month[monthInt];
				} else {
					dayInt = month[monthInt];
				}
			}
		}
		resultDay = "" + yearInt + String.format("%02d", monthInt)
				+ String.format("%02d", dayInt);
		return resultDay;
	}

	/**
	 * 计算相隔几天
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int getDaysBetween(Calendar d1, Calendar d2) {
		if (d1.after(d2)) { // swap dates so that d1 is start and d2 is end
			java.util.Calendar swap = d1;
			d1 = d2;
			d2 = swap;
		}
		int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
		int y2 = d2.get(Calendar.YEAR);
		if (d1.get(Calendar.YEAR) != y2) {
			d1 = (Calendar) d1.clone();
			do {
				days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);// 得到当年的实际天数
				d1.add(Calendar.YEAR, 1);
			} while (d1.get(Calendar.YEAR) != y2);
		}
		return days;
	}

	/**
	 * 计算相隔几天
	 * 如果date1在date2之前则返回正数的days
	 * 如果date1在date2之后则返回负数的days
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int getDaysBetween(Date date1, Date date2) {

		java.util.Calendar d1 = Calendar.getInstance();
		d1.setTimeInMillis(date1.getTime());

		java.util.Calendar d2 = Calendar.getInstance();
		d2.setTimeInMillis(date2.getTime());

		boolean swaped = false;
		if (d1.after(d2)) { // swap dates so that d1 is start and d2 is end
			java.util.Calendar swap = d1;
			d1 = d2;
			d2 = swap;
			swaped = true;
		}
		int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
		int y2 = d2.get(Calendar.YEAR);
		if (d1.get(Calendar.YEAR) != y2) {
			d1 = (Calendar) d1.clone();
			do {
				days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);// 得到当年的实际天数
				d1.add(Calendar.YEAR, 1);
			} while (d1.get(Calendar.YEAR) != y2);
		}
		return swaped ? -days : days;
	}
	
	

	/**
	 * 是否比当前时间至少小1天
	 * 
	 * @param date
	 * @return 如果比当前时间至少小1天则返回true，否则返回flase
	 */
	public static boolean isBetweenAtLeastOneDay(Date date) {

		Calendar cal = Calendar.getInstance();
		int nowYear = cal.get(Calendar.YEAR);
		int nowMonth = cal.get(Calendar.MONTH);
		int nowDay = cal.get(Calendar.DATE);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date);

		int oldYear = cal2.get(Calendar.YEAR);
		int oldMonth = cal2.get(Calendar.MONTH);
		int oldDay = cal2.get(Calendar.DATE);

		if (nowYear < oldYear)
			return false;

		if (nowYear > oldYear)
			return true;

		if (nowMonth > oldMonth) {

			return true;

		} else if (nowMonth == oldMonth) {

			if (nowDay - oldDay >= 1)
				return true;

		}

		return false;

	}
	

	/**
	 * 是否在今天之前
	 * 
	 * @param date
	 * @return
	 */
	public static boolean beforeToday(Date date) {
		return getTodayStartPointer().after(date);
	}

	/**
	 * 是否在今天之前
	 * 
	 * @param date
	 * @return
	 */
	public static boolean beforeTodayS(String date) {
		if (date == null || "".equals(date)) {
			return true;
		}

		try {
			return getTodayStartPointer().after(parse(date, DATE_FORMAT_YYYY_MM_DD));
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 在昨天之前
	 * 
	 * @param dateStr
	 *            yyyy-MM-dd
	 * @return
	 */
	public static boolean beforeYesterday(String dateStr) {
		if (dateStr == null) {
			return false;
		}
		Date date;
		try {
			date = parse(dateStr, DATE_FORMAT_YYYY_MM_DD);
			return getYesterdayStartPointer().after(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	
	/**
	 * 字符串到日期类型 默认格式yyyy-MM-dd HH:mm
	 * 
	 * @param dateString
	 * @return
	 */
	public static Date string2Date(String dateString) {

		try {
			return parse(dateString, "yyyy-MM-dd HH:mm");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 日期到字符串格式 默认格式yyyy-MM-dd HH:mm
	 * 
	 * @param date
	 * @return
	 */
	public static String date2String(Date date) {

		return format(date, "yyyy-MM-dd HH:mm");
	}

	/**
	 * 获取几天之后的日期
	 * 
	 * @param mils
	 * @param day
	 * @return
	 */
	public static long getDaysAfter(long mils, int day) {
		Date d = new Date(mils);
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		return now.getTime().getTime();
	}

	/**
	 * 获取今天的日期字符串 格式yyyy-MM-dd
	 * 
	 * @return 格式yyyy-MM-dd
	 */
	public static String currentDateStr() {
		return format(new Date(), DATE_FORMAT_YYYY_MM_DD);
	}

	/**
	 * 日期按天迭代
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static List<Date> dayIterator(Date startDate, Date endDate) {
		List<Date> dayList = new ArrayList<Date>();
		endDate = getEndDatePointer(endDate);

		Date date = getStartDatePointer(startDate);
		while (date.before(endDate)) {
			dayList.add(date);
			date = addDays(date, 1);
		}

		return dayList;
	}
	
	
	/**
	 * 日期按月迭代
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static List<Date> monthIterator(Date startDate, Date endDate) {
		List<Date> dayList = new ArrayList<Date>();
		endDate = getEndDatePointer(endDate);

		Date date = getStartDatePointer(startDate);
		while (date.before(endDate)) {
			dayList.add(date);
			date = add(date, Calendar.MONTH, 1);
		}

		return dayList;
	}
	

	/**
	 * 日期按天迭代
	 * 
	 * @param startDate
	 *            yyyy-MM-dd
	 * @param endDate
	 *            yyyy-MM-dd
	 * @return
	 */
	public static List<String> dayAfterDay(Date startDate, Date endDate) {
		List<String> dayList = new ArrayList<String>();
		if (startDate.getTime() >= endDate.getTime()) {
			dayList.add(format(startDate, "yyyy-MM-dd"));
		} else {
			while (startDate.before(endDate)) {
				dayList.add(format(startDate, "yyyy-MM-dd"));
				startDate = addDays(startDate, 1);
			}
		}
		return dayList;
	}

	/**
	 * 日期 加(天)
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date addDays(Date date, int amount) {
		return add(date, Calendar.DAY_OF_YEAR, amount);
	}

	/**
	 * 日期加减
	 * 
	 * @param date
	 * @param calendarField
	 * @param amount
	 * @return
	 */
	public static Date add(Date date, int calendarField, int amount) {
		if (date == null) {
			throw new IllegalArgumentException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(calendarField, amount);
		return c.getTime();
	}

	/**
	 * 判断是否为今天
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isToday(Date date) {
		Date start = getTodayStartPointer();
		Date end = getTodayEndPointer();
		return start.compareTo(date) <= 0 && end.compareTo(date) >= 0;
	}
	
	
	/**
	 * 判断是否为同一天
	 * @param dateA
	 * @param dateB
	 * @return
	 */
	public static boolean areSameDay(Date dateA, Date dateB) {
		Calendar calDateA = Calendar.getInstance();
		calDateA.setTime(dateA);

		Calendar calDateB = Calendar.getInstance();
		calDateB.setTime(dateB);

		return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
				&& calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
				&& calDateA.get(Calendar.DAY_OF_MONTH) == calDateB
						.get(Calendar.DAY_OF_MONTH);
	}

	
	/**
	 *当前时间date的  amount分钟后
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date afterMunite(Date date,int amount){
		Calendar cl = Calendar.getInstance();
		cl.setTime(date);
		cl.add(Calendar.MINUTE, amount);
		return cl.getTime();
	}
	
	/**
	 * 获取应用不可达时间
	 * 
	 * @return
	 */
	public static Date getUnreachableDate() {
		Calendar currentDate = new GregorianCalendar();

		currentDate.add(Calendar.YEAR, 200);
		return (Date) currentDate.getTime().clone();
	}

	/**
	 * long转化成Date
	 * @param millis 毫秒
	 * @return
	 */
	public static Date longToDate(long millis){
		Calendar cl = Calendar.getInstance();
		cl.setTimeInMillis(millis);
		return cl.getTime();
	}

	/**
	 * Date转化成long
	 * @param date Date
	 * @return
	 */
	public static long getMillisDate(Date date){
		Calendar cl = Calendar.getInstance();
		cl.setTime(date);
		return cl.getTimeInMillis();
	}

	/**
	 * 是否为同一个月
	 * @param date1 Date
	 * @param date2 Date
	 * @return
	 */
	public static boolean isSameMonth(Date date1,Date date2){
		Date d=getLastMonthStartPointer(date1);
		Date d2=getLastMonthStartPointer(date2);
		return areSameDay(d,d2);
	}
	
}
