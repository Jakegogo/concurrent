package transfer.deserializer;

import transfer.Inputable;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

/**
 * 字符串解析器
 * Created by Jake on 2015/2/25.
 */
public class StringDeserializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.STRING) {
            throw new IllegalTypeException(typeFlag, Types.STRING, type);
        }

        // 读取字符串字节数组的大小
        int length = BitUtils.getInt(inputable);

        byte[] bytes = new byte[length];

        inputable.getBytes(bytes);

        return (T) new String(bytes);
    }


    private static StringDeserializer instance = new StringDeserializer();

    public static StringDeserializer getInstance() {
        return instance;
    }

}
