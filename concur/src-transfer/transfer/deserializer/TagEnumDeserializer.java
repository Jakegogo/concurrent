package transfer.deserializer;

import org.objectweb.asm.MethodVisitor;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.EnumInfo;
import transfer.def.PersistConfig;
import transfer.def.Types;
import transfer.exceptions.IllegalClassTypeException;
import transfer.exceptions.IllegalTypeException;
import transfer.exceptions.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * 带标签的枚举解析器
 * Created by Jake on 2015/2/25.
 */
public class TagEnumDeserializer implements Deserializer {


    /**
     * 枚举名解析器
     */
    private static final ShortStringDeserializer STRING_DESERIALIZER = ShortStringDeserializer.getInstance();


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = PersistConfig.getType(flag);
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
            rawClass = PersistConfig.getClass(enumType);
        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(type);
        }

        EnumInfo enumInfo = (EnumInfo) PersistConfig.getOrCreateClassInfo(rawClass);

        if (enumInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        if (enumType != enumInfo.getClassId()) {
            throw new IllegalClassTypeException(enumType, type);
        }

        // 读取枚举名
        String enumName = STRING_DESERIALIZER.deserialze(inputable, String.class, inputable.getByte(), referenceMap);
        return (T) enumInfo.toEnum(enumName);// 不存在的枚举则返回null
    }

    
    @Override
	public void compile(Type type, MethodVisitor mw,
			AsmDeserializerContext context) {
    	
	}

    private static TagEnumDeserializer instance = new TagEnumDeserializer();

    public static TagEnumDeserializer getInstance() {
        return instance;
    }

}
