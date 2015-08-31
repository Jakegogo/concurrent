package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.EnumInfo;
import transfer.core.SerialContext;
import transfer.def.PersistConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;

import java.lang.reflect.Type;

/**
 * 带标签枚举编码器 Created by Jake on 2015/2/26.
 */
public class TagEnumSerializer implements Serializer {

	// 标签编码器
	private static final ShortStringSerializer STRING_SERIALIZER = ShortStringSerializer
			.getInstance();

	@Override
	public void serialze(Outputable outputable, Object object,
			SerialContext context) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, context);
			return;
		}

		outputable.putByte(Types.ENUM);
		Enum<?> enumVal = (Enum<?>) object;

		EnumInfo enumInfo = (EnumInfo) PersistConfig.getOrCreateClassInfo(enumVal.getDeclaringClass());
		BitUtils.putInt(outputable, enumInfo.getClassId());
		String enumName = enumInfo.toString(enumVal);
		// 添加标签
		STRING_SERIALIZER.serialze(outputable, enumName, context);

	}

	@Override
	public void compile(Type type, MethodVisitor mw,
			AsmSerializerContext context) {

	}

	private static final TagEnumSerializer instance = new TagEnumSerializer();

	public static TagEnumSerializer getInstance() {
		return instance;
	}

}
