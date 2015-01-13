package dbcache.utils;

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
	

}
