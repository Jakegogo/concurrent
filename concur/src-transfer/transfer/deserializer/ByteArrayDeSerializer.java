package transfer.deserializer;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

import org.objectweb.asm.MethodVisitor;

/**
 * 字节数组解析器
 * Created by Jake on 2015/2/24.
 */
public class ByteArrayDeSerializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.BYTE_ARRAY) {
            throw new IllegalTypeException(typeFlag, Types.BYTE_ARRAY, type);
        }

        // 读取字节数组的大小
        int length = BitUtils.getInt(inputable);

        byte[] bytes = new byte[length];

        inputable.getBytes(bytes);

        return (T) bytes;
    }

    
    @Override
	public void compile(Type type, MethodVisitor mw,
			AsmDeserializerContext context) {
    	
	}

    private static ByteArrayDeSerializer instance = new ByteArrayDeSerializer();

    public static ByteArrayDeSerializer getInstance() {
        return instance;
    }

}
