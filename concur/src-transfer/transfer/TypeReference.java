package transfer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 类型定义
 * @param <T>
 */
public class TypeReference<T> {

    private final Type type;

    protected TypeReference(){
        Type superClass = getClass().getGenericSuperclass();

        if (superClass instanceof ParameterizedType) {
            type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        } else {
            type = superClass;
        }
    }

    public Type getType() {
        return type;
    }
    
    public final static Type LIST_STRING = new TypeReference<List<String>>() {}.getType();

}
