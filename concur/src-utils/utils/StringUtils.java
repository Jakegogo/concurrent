package utils;

/**
 * String 工具类合集
 * 
 * @author Jake
 *
 */
public class StringUtils {

	
	/**
	 * 获取首字母小写的字符串
	 * @param str 源字符串
	 * @return 替换后的字符串
	 */
	public static String getLString(String str) {
		if(str == null || str.length() == 0) {
			return str;
		}
		char c = str.charAt(0);
		if (c >= 'A' && c <= 'Z') {
			c = (char) (c + 32); 
		} else {
			return str;
		}
		int len = str.length();
		char[] chars = new char[len];
		str.getChars(1, len, chars, 1);
		chars[0] = c;
 		return new String(chars);
	}
	
	
	/**
	 * 获取首字母大写的字符串
	 * @param str 源字符串
	 * @return 替换后的字符串
	 */
	public static String getUString(String str) {
		if(str == null || str.length() == 0) {
			return str;
		}
		char c = str.charAt(0);
		if (c >= 'a' && c <= 'z') {
			c = (char) (c - 32); 
		} else {
			return str;
		}
		int len = str.length();
		char[] chars = new char[len];
		str.getChars(1, len, chars, 1);
		chars[0] = c;
 		return new String(chars);
	}


	/**
	 * 判断字符串是否为空
	 * @param str 目标字符串
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	
	/**
	 * 判断字符串是否为空
	 * @param str 目标字符串
	 * @return
	 */
	public static boolean isBlank(String str) {
		return str == null || str.trim().length() == 0;
	}

}
