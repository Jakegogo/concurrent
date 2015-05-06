package transfer.core;

import utils.enhance.asm.AsmAccessHelper;
import utils.enhance.asm.ValueGetter;
import utils.enhance.asm.ValueSetter;
import utils.enhance.asm.EnhanceAccessException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * 属性信息
 * Created by Jake on 2015/2/23.
 */
public class FieldInfo<T> {

    /**
     * 属性信息所属类
     */
    private Class<T> clazz;

    /**
     * 属性信息
     */
    private String name;

    /**
     * 属性名
     */
    private String fieldName;
    
    /**
     * 属性
     */
    private Field field;

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
    public static <T> FieldInfo<T> valueOf(Class<T> clazz, Field field) {
        FieldInfo<T> fieldInfo = new FieldInfo<T>();
        fieldInfo.clazz = clazz;
        fieldInfo.name = clazz.getName() + "#" + field.getName();
        fieldInfo.fieldName = field.getName();
        fieldInfo.type = field.getGenericType();
        fieldInfo.field = field;
        return fieldInfo;
    }
    
    public Class<T> getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    protected ValueGetter<T> getFieldGetter() {
        if (this.fieldGetter == null) {
            try {
                this.fieldGetter = AsmAccessHelper.createFieldGetter(name, clazz, field);
            } catch (Exception e) {
                throw new EnhanceAccessException("无法创建字节码增强属性获取器:" + name, e);
            }
        }
        return fieldGetter;
    }

    protected ValueSetter<T> getFieldSetter() {
        if (this.fieldSetter == null) {
            try {
                this.fieldSetter = AsmAccessHelper.createFieldSetter(name, clazz, field);
            } catch (Exception e) {
                throw new EnhanceAccessException("无法创建字节码增强属性获取器:" + name, e);
            }
        }
        return fieldSetter;
    }

    public Type getType() {
        return type;
    }

    public void setField(T object, Object fieldValue) {
        this.getFieldSetter().set(object, fieldValue);
    }

    public Object getField(T object) {
        return this.getFieldGetter().get(object);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Field getField() {
        return field;
    }
}
