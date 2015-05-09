package transfer.deserializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.DeserialContext;

import java.lang.reflect.Type;

/**
 * 类型解析器接口
 * Created by Jake on 2015/2/23.
 */
public interface Deserializer {


    /**
     * 解析方法
     * @param context 解析上下文
     * @param <T>
     * @param inputable 输入接口
     * @param type 类型
     * @param flag 类型byte
     * @param context
     * @return
     */
    <T> T deserialze(Inputable inputable, Type type, byte flag, DeserialContext context);


    /**
     * 预编译编码方法
     * @param type
     * @param mw serialze方法MethodVisitor
     * @param context
     */
    void compile(Type type, MethodVisitor mw, AsmDeserializerContext context);
    
}
