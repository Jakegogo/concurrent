package utils.enhance.asm;

/**
 * 抽象属性设值器
 * Created by Jake on 2015/1/1.
 * @param <T> 实体类型
 */
public abstract class AbstractFieldSetter<T> implements ValueSetter<T>, Cloneable {

    // asm重写
    @Override
    public abstract void set(T target, Object object);


    @Override
    public abstract String getName();


    @Override
    @SuppressWarnings("unchecked")
    public ValueSetter<T> doClone() {
        try {
            return (ValueSetter<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
