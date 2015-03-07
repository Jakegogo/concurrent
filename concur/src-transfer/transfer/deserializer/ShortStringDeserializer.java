package transfer.deserializer;

import transfer.Inputable;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

/**
 * 短字符串解析器 最大长度255
 * Created by Jake on 2015/2/25.
 */
public class ShortStringDeserializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.STRING) {
            throw new IllegalTypeException(typeFlag, Types.STRING, type);
        }

        // 读取字符串字节数组的大小
        int length = BitUtils.getInt1(inputable);

        byte[] bytes = new byte[length];

        inputable.getBytes(bytes);

        return (T) new String(bytes);
    }


    private static ShortStringDeserializer instance = new ShortStringDeserializer();

    public static ShortStringDeserializer getInstance() {
        return instance;
    }

}
