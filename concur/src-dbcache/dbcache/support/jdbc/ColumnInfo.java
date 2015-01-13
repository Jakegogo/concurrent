package dbcache.support.jdbc;

import java.lang.reflect.Field;

import dbcache.support.asm.AsmAccessHelper;
import dbcache.support.asm.ValueGetter;
import dbcache.support.asm.ValueSetter;

/**
 * 字段信息 
 * Created by Jake on 2015/1/12.
 */
public class ColumnInfo<T> {

	private ValueGetter<T> attrGetter;

	private ValueSetter<T> attrSetter;

	/**
	 * 获取实例
	 * 
	 * @param field
	 *            属性
	 * @return
	 * @throws Exception 
	 */
	public static <T> ColumnInfo<T> valueOf(Class<T> clazz, Field field) throws Exception {
		ColumnInfo<T> columnInfo = new ColumnInfo<T>();
		columnInfo.attrGetter = AsmAccessHelper.createFieldGetter(clazz, field);
		columnInfo.attrSetter = AsmAccessHelper.createFieldSetter(clazz, field);
		return columnInfo;
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
	 * 设置属性值
	 * @param object 实体
	 * @param value 属性值
	 */
	public void setValue(T object, Object value) {
		this.attrSetter.set(object, value);
	}

}
