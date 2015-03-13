package transfer.deserializer;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.MethodVisitor;

/**
 * Decimal解析器
 * Created by Jake on 2015/2/24.
 */
public class DecimalDeserializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.DECIMAL) {
            throw new IllegalTypeException(typeFlag, Types.DECIMAL, type);
        }

        byte extraFlag = TransferConfig.getExtra(flag);

        Number number = null;
        switch (extraFlag) {
            case TransferConfig.FLOAT:
                number = BitUtils.getFloat(inputable);
                if (type == float.class || type == Float.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.DOUBLE:
                number = BitUtils.getDouble(inputable);
                if (type == double.class || type == Double.class) {
                    return (T) number;
                }
                break;
        }

        if (type == null || type == Number.class) {
            return (T) number;
        }

        if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(number.floatValue());
        } else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(number.doubleValue());
        } else if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(number.shortValue());
        } else if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(number.intValue());
        } else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(number.longValue());
        } else if (type == byte.class || type == Byte.class) {
            return (T) TypeUtils.castToByte(number);
        } else if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(number.intValue() == 1);
        } else if (type == AtomicInteger.class) {
            return (T) new AtomicInteger(number.intValue());
        } else if (type == AtomicLong.class) {
            return (T) new AtomicLong(number.longValue());
        }

        return (T) number;
    }

    
    @Override
	public void compile(Type type, MethodVisitor mw,
			AsmDeserializerContext context) {
    	
	}

    private static DecimalDeserializer instance = new DecimalDeserializer();

    public static DecimalDeserializer getInstance() {
        return instance;
    }

}
