package utils.enhance.asm;

/**
 * 属性值设置器
 * Created by Jake on 2015/1/1.
 */
public interface ValueSetter<T> {


    /**
     * 设置值
     * @param target 目标实体
     * @param object 目标值
     * @return 返回值
     */
    public void set(T target, Object object);

    /**
     * 获取值的名称
     * @return
     */
    public String getName();

    /**
     * 克隆对象
     * @return
     */
    public ValueSetter<T> doClone();


}
