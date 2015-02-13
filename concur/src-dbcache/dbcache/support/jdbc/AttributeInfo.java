package dbcache.support.jdbc;

import com.alibaba.fastjson.JSON;
import dbcache.annotation.JsonType;
import dbcache.support.asm.AsmAccessHelper;
import dbcache.support.asm.ValueGetter;
import dbcache.support.asm.ValueSetter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * 实体属性信息
 * Created by Jake on 2015/1/12.
 */
public class AttributeInfo<T> {

	/**
	 * 属性名
	 */
	private String name;

	/**
	 * 字段名
	 */
	private String columnName;

	/**
	 * 属性获值器
	 */
	private ValueGetter<T> attrGetter;

	/**
	 * 属性设值器
	 */
	private ValueSetter<T> attrSetter;

	/**
	 * 序号
	 */
	private int index;

	/**
	 * sql字段类型
	 */
	private int sqlType;

	/**
	 * 是否为主键
	 */
	private boolean isPrimaryKey = false;
	
	/**
	 * 属性类型
	 */
	private Class<?> type;

	/**
	 * 是否为json属性
	 */
	private boolean jsonType;

	/**
	 * json属性类型
	 */
	private Type targetType;


	/**
	 * 获取实例
	 * @param clazz 实体类
	 * @param field 属性
	 * @param columnName 表字段名
	 * @param index 序号
	 * @return
	 * @throws Exception
	 */
	public static <T> AttributeInfo<T> valueOf(Class<T> clazz, Field field, String columnName, int index) throws Exception {
		AttributeInfo<T> columnInfo = new AttributeInfo<T>();
		columnInfo.name = field.getName();
		columnInfo.setColumnName(columnName);
		columnInfo.attrGetter = AsmAccessHelper.createFieldGetter(clazz, field);
		columnInfo.attrSetter = AsmAccessHelper.createFieldSetter(clazz, field);
		columnInfo.index = index;
		columnInfo.type = field.getDeclaringClass();
		columnInfo.targetType = field.getGenericType();

		// 处理Json转换注解
		if (field.isAnnotationPresent(JsonType.class)) {
			columnInfo.jsonType = true;
		}

		return columnInfo;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ValueGetter<T> getAttrGetter() {
		return attrGetter;
	}

	public ValueSetter<T> getAttrSetter() {
		return attrSetter;
	}

	/**
	 * 获取属性值
	 * @param object 实体
	 * @return
	 */
	public Object getValue(T object) {
		return this.attrGetter.get(object);
	}

	/**
	 * 获取持久化属性
	 * @param object
	 * @return
	 */
	public Object getPersistValue(T object) {
		if (jsonType) {
			Object objectValue = this.getValue(object);
			if (objectValue != null) {
				return JSON.toJSONString(objectValue);
			}
			return null;
		}
		return this.getValue(object);
	}

	/**
	 * 设置属性值
	 * @param object 实体
	 * @param value 属性值
	 */
	public void setValue(T object, Object value) {
		this.attrSetter.set(object, value);
	}

	/**
	 * 从持久化字段值设置属性值
	 * @param object 实体
	 * @param value 属性值
	 */
	public void setFromPersistValue(T object, Object value) {
		if (jsonType) {
			if (value == null) {
				this.setValue(object, null);
			} else {
				// 转化成对象
				this.setValue(object, JSON.parseObject(value.toString(), this.targetType));
			}
			return;
		}
		this.setValue(object, value);
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public boolean isJsonType() {
		return jsonType;
	}

	public Type getTargetType() {
		return targetType;
	}

}
