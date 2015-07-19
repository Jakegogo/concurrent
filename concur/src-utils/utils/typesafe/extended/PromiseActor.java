package utils.typesafe.extended;

import java.util.ArrayList;
import java.util.List;

/**
 * 业务逻辑的原子操作
 * Created by Jake on 2015/7/19.
 */
public abstract class PromiseActor<T> implements Promiseable<T> {

    private List<MultiSafeType> safeTypes = new ArrayList<MultiSafeType>();

    /**
     * 获取操作相关类型,必要时候重写
     * <br/>(多对象修改在一个原子性操作的时候)
     * @return
     */
    @Override
    public MultiSafeType[] promiseTypes() {
        return new MultiSafeType[]{(MultiSafeType) this.get()};
    }

    /**
     * 当获取所需要的关联操作后,when代表将子操作纳入本操作组成新的原子性操作
     * @param promiseables
     * @return
     */
    public void when(Promiseable... promiseables) {
        if (promiseables == null || promiseables.length == 0) {
            throw new IllegalArgumentException("promiseables must more than one arguments");
        }

        for (int i = 0;i < promiseables.length;i++) {
            Promiseable<?> promiseable = promiseables[i];
            for (MultiSafeType safeType : promiseable.promiseTypes()) {
                safeTypes.add(safeType);
            }
        }
    }

}
