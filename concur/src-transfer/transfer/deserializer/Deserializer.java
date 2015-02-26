package transfer.deserializer;

import transfer.Inputable;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

/**
 * 类型解析器接口
 * Created by Jake on 2015/2/23.
 */
public interface Deserializer {


    /**
     * 解析方法
     * @param inputable 输入接口
     * @param type 类型
     * @param flag 类型byte
     * @param referenceMap 引用表
     * @param <T>
     * @return
     */
    <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap);


}
