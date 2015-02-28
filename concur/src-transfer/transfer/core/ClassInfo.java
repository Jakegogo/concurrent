package transfer.core;

import java.util.List;

/**
 * 类信息
 * Created by Jake on 2015/2/23.
 */
public class ClassInfo {

    /**
     * 传输类唯一标识
     */
    private int classId;

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
     * @param clazz 类
     * @param classId 传输类Id
     * @param fieldInfos 属性信息
     * @return
     */
    public static ClassInfo valueOf(Class<?> clazz, int classId, List<FieldInfo> fieldInfos) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.clazz = clazz;
        classInfo.classId = classId;
        classInfo.fieldInfos = fieldInfos;
        return classInfo;
    }


    public Class<?> getClazz() {
        return clazz;
    }

    public List<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }

    public int getClassId() {
        return classId;
    }

}
