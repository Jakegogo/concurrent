package utils.typesafe.extended;

/**
 * 线程安全的返回值
 * Created by Jake on 2015/7/12.
 */
public interface Promiseable<T> {

    /**
     * 获取值
     * @return
     */
    public T get();

    /**
     * 执行并获取值
     * @return
     */
    public T call();

    /**
     * 获取操作相关类型
     * @return
     */
    public MultiSafeType[] promiseTypes();

}
