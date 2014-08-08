package dbcache.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * 泛型工具类
 */
public class GenericsUtils {
	
	/**
	 * 获得指定类的父类的泛型参数的实际类型
	 * @param clazz Class
	 * @param index  泛型参数所在索引,从0开始
	 * @return  Class
	 */
	@SuppressWarnings("rawtypes")
	public static Class getSuperClassGenricType(Class clazz, int index) {
		if (clazz == null) {
			return null;
		}
		
		Type genericType = clazz.getGenericSuperclass();
		while (genericType != null && !(genericType instanceof ParameterizedType)) {
			clazz = clazz.getSuperclass();
			if (clazz == null) {
				break;
			} else {
				genericType = clazz.getGenericSuperclass();
			}
		}

		if (!(genericType instanceof ParameterizedType)) {
			return Object.class;
		}
		
		Type[] params = ((ParameterizedType) genericType).getActualTypeArguments();
		if (params != null && index >= 0 && index < params.length ) {
			if (params[index] instanceof Class) {
				return (Class) params[index];
			}
		}
		
		return Object.class;
	}
	

}
