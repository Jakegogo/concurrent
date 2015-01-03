package dbcache.conf;

import com.alibaba.fastjson.JSON;
import dbcache.support.asm.AsmAccessHelper;
import dbcache.support.asm.ValueGetter;
import dbcache.support.asm.ValueSetter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Json属性自动转换信息
 * @param T 实体类型
 * Created by Jake on 2015/1/1.
 */
public class JsonConverter<T> implements Cloneable {


    /**
     * 源属性值获取器
     */
    private ValueGetter<T> sourceGetter;

    /**
     * 源属性值设值器
     */
    private ValueSetter<T> sourceSetter;


    /**
     * 目标属性类型
     */
    private Type targetType;


    /**
     * 目标属性获取器
     */
    private ValueGetter<T> targetGetter;

    /**
     * 目标属性设值器
     */
    private ValueSetter<T> targetSetter;


    public JsonConverter() {
    }


    /**
     * 生成实例
     *
     * @param clz 实体类
     * @param field 目标属性
     * @param value json串属性名
     * @return
     */
    public static <T> JsonConverter valueof(Class<T> clz, Field field, String value) throws Exception {
        JsonConverter jsonConvertConfig = new JsonConverter();
        jsonConvertConfig.sourceGetter = AsmAccessHelper.createFieldGetter(clz, clz.getDeclaredField(value));
        jsonConvertConfig.sourceSetter = AsmAccessHelper.createFieldSetter(clz, clz.getDeclaredField(value));
        jsonConvertConfig.targetType = field.getGenericType();
        jsonConvertConfig.targetGetter = AsmAccessHelper.createFieldGetter(clz, field);
        jsonConvertConfig.targetSetter = AsmAccessHelper.createFieldSetter(clz, field);
        return jsonConvertConfig;
    }


    /**
     * 克隆
     * @return
     */
    public JsonConverter<T> clone() {
        try {
            JsonConverter<T> instance = (JsonConverter<T>) super.clone();
            instance.sourceGetter = this.sourceGetter.clone();
            instance.targetSetter = this.targetSetter.clone();
            return instance;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行JsonString -> Object 转换
     *  @param target 目标实体
     */
    public void doConvert(T target) {
        // 获取Json字符串
        Object object = sourceGetter.get(target);
        if (object == null) {
            return;
        }
        String jsonString = object.toString();
        // 转换Json
        Object targetObject = JSON.parseObject(jsonString, this.targetType);
        // 设值到目标属性
        targetSetter.set(target, targetObject);
    }

    /**
     * 进行Object -> JsonString 转换
     * @param target 目标实体
     */
    public void doPersist(T target) {
        // 获取属性值
        Object object = targetGetter.get(target);
        if(object == null) {
            sourceSetter.set(target, null);
            return;
        }
        // 转换成Json
        Object jsonObject = JSON.toJSON(object);
        // 设值到json串属性
        sourceSetter.set(target, jsonObject == null ? null : jsonObject.toString());
    }


    public ValueSetter<T> getTargetSetter() {
        return targetSetter;
    }

    public ValueGetter<T> getSourceGetter() {
        return sourceGetter;
    }

    public Type getTargetType() {
        return targetType;
    }

    public ValueSetter<T> getSourceSetter() {
        return sourceSetter;
    }

    public ValueGetter<T> getTargetGetter() {
        return targetGetter;
    }
}
