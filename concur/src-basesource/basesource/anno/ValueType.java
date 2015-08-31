package basesource.anno;

import basesource.validators.ValueGetter;

/**
 * <h3>值类型注解</h3>
 * <p>声明字段值类型,支持json，可以自定义ValueGetter</p>
 * <p>默认直接取字段值</p>
 * Created by Jake on 2015/6/24.
 */
public @interface ValueType {

    /**
     * 值(json格式的值使用:{value}.xxx.xx)
     * @return
     */
    String[] value() default basesource.contants.ValueType.VALUE;

    /**
     * 获取值的类
     * @return
     */
    Class<? extends ValueGetter> valueGetter() default ValueGetter.class;

}
