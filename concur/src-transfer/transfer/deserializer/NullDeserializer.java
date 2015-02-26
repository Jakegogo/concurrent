package transfer.deserializer;

import transfer.Inputable;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

/**
 * NULL解析器
 * Created by Jake on 2015/2/24.
 */
public class NullDeserializer implements Deserializer {


    public static void setInstance(NullDeserializer instance) {
        NullDeserializer.instance = instance;
    }

    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {
        return null;
    }


    private static NullDeserializer instance = new NullDeserializer();

    public static NullDeserializer getInstance() {
        return instance;
    }

}
