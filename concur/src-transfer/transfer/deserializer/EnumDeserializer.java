package transfer.deserializer;

import org.objectweb.asm.MethodVisitor;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.EnumInfo;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalClassTypeException;
import transfer.exception.IllegalTypeException;
import transfer.exception.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * 枚举解析器
 * Created by Jake on 2015/2/25.
 */
public class EnumDeserializer implements Deserializer {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.ENUM) {
            throw new IllegalTypeException(typeFlag, Types.ENUM, type);
        }

        // 读取枚举类型
        int enumType = BitUtils.getInt(inputable);

        Class<?> rawClass = TypeUtils.getRawClass(type);

        if (type == null 
        		|| type == Object.class 
        		|| type == Enum.class
        		|| rawClass.isInterface()
				|| Modifier.isAbstract(rawClass.getModifiers()) && !rawClass.isArray()) {

            rawClass = TransferConfig.getClass(enumType);

        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(type);
        }

        EnumInfo enumInfo = (EnumInfo) TransferConfig.getOrCreateClassInfo(rawClass);

        if (enumInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        if (enumType != enumInfo.getClassId()) {
            throw new IllegalClassTypeException(enumType, type);
        }

        // 读取枚举索引
        int enumIndex = BitUtils.getInt(inputable);

        return (T) enumInfo.toEnum(enumIndex);
    }

    
    @Override
	public void compile(Type type, MethodVisitor mw,
			AsmDeserializerContext context) {
    	
	}
    

    private static EnumDeserializer instance = new EnumDeserializer();

    public static EnumDeserializer getInstance() {
        return instance;
    }

}
