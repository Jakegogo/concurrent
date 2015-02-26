package transfer.core;

import java.util.List;

/**
 * 类信息
 * Created by Jake on 2015/2/23.
 */
public class ClassInfo {

    /**
     * 类
     */
    protected Class<?> clazz;

    /**
     * 属性列表
     */
    @SuppressWarnings("rawtypes")
    protected List<FieldInfo> fieldInfos;

    /**
     * 获取实例
     * @param clazz
     * @param fieldInfos
     * @return
     */
    public static ClassInfo valueOf(Class<?> clazz, List<FieldInfo> fieldInfos) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.clazz = clazz;
        classInfo.fieldInfos = fieldInfos;
        return classInfo;
    }


    public Class<?> getClazz() {
        return clazz;
    }

    public List<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }
}
