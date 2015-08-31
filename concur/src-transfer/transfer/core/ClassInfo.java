package transfer.core;

import java.util.List;
import java.util.Map;

/**
 * 类信息
 * Created by Jake on 2015/2/23.
 */
public class ClassInfo {

    /**
     * 传输类唯一标识
     */
    int classId;

    /**
     * 类
     */
    Class<?> clazz;

    /**
     * 属性列表
     */
    @SuppressWarnings("rawtypes")
    private List<FieldInfo> fieldInfos;

    /**
     * 属性Map
     */
    private Map<String, FieldInfo> fieldInfoMap;

    /**
     * 获取实例
     * @param clazz 类
     * @param classId 传输类Id
     * @param fieldInfos 属性信息
     * @param fieldInfoMap
     * @return
     */
    public static ClassInfo valueOf(Class<?> clazz, int classId, List<FieldInfo> fieldInfos, Map<String, FieldInfo> fieldInfoMap) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.clazz = clazz;
        classInfo.classId = classId;
        classInfo.fieldInfos = fieldInfos;
        classInfo.fieldInfoMap = fieldInfoMap;
        return classInfo;
    }


    public Class<?> getClazz() {
        return clazz;
    }

    public List<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }

    public Map<String, FieldInfo> getFieldInfoMap() {
        return fieldInfoMap;
    }

    public FieldInfo getFieldInfo(String fieldName) {
        return fieldInfoMap.get(fieldName);
    }

    public int getClassId() {
        return classId;
    }

}
