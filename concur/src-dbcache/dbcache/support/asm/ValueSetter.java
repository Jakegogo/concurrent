package dbcache.support.asm;

/**
 * 属性值设置器
 * Created by Jake on 2015/1/1.
 */
public interface ValueSetter<T> {


    /**
     * 设置真实对象
     * @param object
     */
    public void setTarget(T object);

    /**
     * 设置值
     * @param object 目标值
     * @return 返回值
     */
    public void set(Object object);

    /**
     * 获取值的名称
     * @return
     */
    public String getName();

    /**
     * 克隆对象
     * @return
     */
    public ValueSetter<T> clone();


}
