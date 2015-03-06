package transfer.core;

import dbcache.support.asm.AsmAccessHelper;
import dbcache.support.asm.ValueGetter;
import dbcache.support.asm.ValueSetter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * 属性信息
 * Created by Jake on 2015/2/23.
 */
public class FieldInfo<T> {

    /**
     * 属性信息
     */
    private String name;

    /**
     * 属性名
     */
    private String fieldName;

    /**
     * 属性获值器
     */
    private ValueGetter<T> fieldGetter;

    /**
     * 属性设值器
     */
    private ValueSetter<T> fieldSetter;

    /**
     * 属性类型
     */
    private Type type;


    /**
     * 获取实例
     * @param clazz
     * @param field
     * @param <T>
     * @return
     */
    public static <T> FieldInfo<T> valueOf(Class<T> clazz, Field field) throws Exception {
        FieldInfo<T> fieldInfo = new FieldInfo<T>();
        fieldInfo.name = clazz.getName() + "#" + field.getName();
        fieldInfo.fieldName = field.getName();
        fieldInfo.fieldGetter = AsmAccessHelper.createFieldGetter(clazz, field);
        fieldInfo.fieldSetter = AsmAccessHelper.createFieldSetter(clazz, field);
        fieldInfo.type = field.getGenericType();
        return fieldInfo;
    }


    public String getName() {
        return name;
    }

    public ValueGetter<T> getFieldGetter() {
        return fieldGetter;
    }

    public ValueSetter<T> getFieldSetter() {
        return fieldSetter;
    }

    public Type getType() {
        return type;
    }

    public void setField(T object, Object fieldValue) {
        this.fieldSetter.set(object, fieldValue);
    }

    public Object getField(T object) {
        return this.fieldGetter.get(object);
    }

    public String getFieldName() {
        return fieldName;
    }
}
