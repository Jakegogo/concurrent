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
 * Number解析器
 * Created by Jake on 2015/2/24.
 */
public class NumberDeserializer implements Deserializer {

    // 0000 0111 数字类型
    public static final byte NUMBER_MASK = (byte) 0x07;

    // 0000 1000
    public static final byte FLAG_NEGATIVE = (byte) 0x08;


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.NUMBER) {
            throw new IllegalTypeException(typeFlag, Types.NUMBER, type);
        }

        byte extraFlag = TransferConfig.getExtra(flag);
        // 符号
        int sign = (extraFlag & FLAG_NEGATIVE) > 0 ? -1 : 1;

        byte len = (byte) (extraFlag & NUMBER_MASK);

        Number number = null;
        switch (len) {
            case TransferConfig.INT1:
                number = BitUtils.getInt1(inputable) * sign;
                if (type == int.class || type == Integer.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.INT2:
                number = BitUtils.getInt2(inputable) * sign;
                if (type == int.class || type == Integer.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.INT3:
                number = BitUtils.getInt3(inputable) * sign;
                if (type == int.class || type == Integer.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.INT4:
                number = BitUtils.getInt(inputable) * sign;
                if (type == int.class || type == Integer.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.INT5:
                number = BitUtils.getLong5(inputable) * sign;
                if (type == long.class || type == Long.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.INT6:
                number = BitUtils.getLong6(inputable) * sign;
                if (type == long.class || type == Long.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.INT7:
                number = BitUtils.getLong7(inputable) * sign;
                if (type == long.class || type == Long.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.INT8:
                number = BitUtils.getLong(inputable) * sign;
                if (type == long.class || type == Long.class) {
                    return (T) number;
                }
                break;
        }

        if (type == null || type == Number.class) {
            return (T) number;
        }

        if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(number.shortValue());
        } else if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(number.intValue());
        } else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(number.longValue());
        } else  if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(number.intValue() == 1);
        } else if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(number.floatValue());
        } else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(number.doubleValue());
        } else if (type == byte.class || type == Byte.class) {
            return (T) TypeUtils.castToByte(number);
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
    

    private static NumberDeserializer instance = new NumberDeserializer();

    public static NumberDeserializer getInstance() {
        return instance;
    }

}
