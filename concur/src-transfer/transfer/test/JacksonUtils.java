package transfer.test;

import java.lang.reflect.Type;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jackson工具类
 * 
 * @author bingshan
 */
public abstract class JacksonUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);
	
	/**
	 * jackson ObjectMapper
	 */
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * jackson TypeFactory
	 */
	private static final TypeFactory typeFactory = TypeFactory.defaultInstance();
	
	/**
	 * 对象转换成json字符串
	 * @param obj Object
	 * @return String
	 */
	public static String object2JsonString(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception ex) {
			String msg = String.format("对象转换成json字符串异常: %s", ex.getMessage());
			logger.error(msg, ex);
			return null;
		}
	}
	
	/**
	 * json字符串转换成对象
	 * @param jsonString String
	 * @param valueType 对象类型
	 * @return T
	 */
	public static <T> T jsonString2Object(String jsonString, Class<T> valueType) {
		try {
			return mapper.readValue(jsonString, valueType);
		} catch (Exception ex) {
			String msg = String.format("json字符串转换成对象异常: %s\t\t json字符串: %s", ex.getMessage(), jsonString);
			logger.error(msg, ex);
			return null;
		}
	}
	
	/**
	 * json字符串转换成对象
	 * @param jsonString String
	 * @param valueTypeRef 对象类型
	 * @return T
	 */
	public static <T> T jsonString2Object(String jsonString, TypeReference<T> valueTypeRef) {
		try {
			return mapper.readValue(jsonString, valueTypeRef);
		} catch (Exception ex) {
			String msg = String.format("json字符串转换成对象异常: %s\t\t json字符串: %s", ex.getMessage(), jsonString);
			logger.error(msg, ex);
			return null;
		} 
	}
	
	/**
	 * 对象转换成字节数组
	 * @param obj Object
	 * @return byte[]
	 */
	public static byte[] object2Bytes(Object obj) {
		try {
			return mapper.writeValueAsBytes(obj);
		} catch (Exception ex) {
			String msg = String.format("对象转换成字节数组异常: %s", ex.getMessage());
			logger.error(msg, ex);
			return null;
		}
	}
	
	/**
	 * 字节数组转换成对象
	 * @param data  字节数组
	 * @param valueType JavaType
	 * @return Object
	 */
	public static Object bytes2Object(byte[] data, JavaType valueType) {
		if (data == null) {
			return null;
		}
		
		try {
			return mapper.readValue(data, 0, data.length, valueType);
		} catch (Exception ex) {
			String msg = String.format("字节数组转换成对象异常: %s", ex.getMessage());
			logger.error(msg, ex);
			return null;
		}		
	}
	
	/**
	 * 字节数组转换成对象
	 * @param data 字节数组
	 * @param valueType Type
	 * @return Object
	 */
	public static Object bytes2Object(byte[] data, Type valueType) {
		if (data == null) {
			return null;
		}
		
		try {
			return mapper.readValue(data, 0, data.length, typeFactory.constructType(valueType));
		} catch (Exception ex) {
			String msg = String.format("字节数组转换成对象异常: %s", ex.getMessage());
			logger.error(msg, ex);
			return null;
		}		
	}
	
	/**
	 * 字节数组转换成对象
	 * @param data 字节数组
	 * @param valueTypeRef TypeReference
	 * @return Object
	 */
	public static <T> T bytes2Object(byte[] data, TypeReference<T> valueTypeRef) {
		if (data == null) {
			return null;
		}
		
		try {
			return mapper.readValue(data, valueTypeRef);
		} catch (Exception ex) {
			String msg = String.format("字节数组转换成对象异常: %s", ex.getMessage());
			logger.error(msg, ex);
			return null;
		}		
	}
	
}
