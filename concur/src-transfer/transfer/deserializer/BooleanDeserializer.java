package transfer.deserializer;

import transfer.Inputable;
import transfer.def.Config;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

/**
 * 布尔解析器
 * Created by Jake on 2015/2/25.
 */
public class BooleanDeserializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = Config.getType(flag);

        if (typeFlag != Types.BOOLEAN) {
            throw new IllegalTypeException(typeFlag, Types.BOOLEAN, type);
        }

        byte extraFlag = Config.getExtra(flag);
        if (extraFlag == 0x01) {
            return (T) Boolean.valueOf(true);
        }

        return (T) Boolean.valueOf(false);
    }


    private static BooleanDeserializer instance = new BooleanDeserializer();

    public static BooleanDeserializer getInstance() {
        return instance;
    }

}
