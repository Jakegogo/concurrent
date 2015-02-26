package transfer.deserializer;

import transfer.Inputable;
import transfer.def.Config;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * 日期解析器
 * Created by Jake on 2015/2/25.
 */
public class DateDeserializer implements Deserializer {


    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = Config.getType(flag);

        if (typeFlag != Types.DATE_TIME) {
            throw new IllegalTypeException(typeFlag, Types.DATE_TIME, type);
        }

        long timestamp = BitUtils.getLong(inputable);

        return (T) new Date(timestamp);
    }


    private static DateDeserializer instance = new DateDeserializer();

    public static DateDeserializer getInstance() {
        return instance;
    }

}
