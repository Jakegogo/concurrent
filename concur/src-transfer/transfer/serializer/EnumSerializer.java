package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.EnumInfo;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;

/**
 * 枚举编码器 Created by Jake on 2015/2/26.
 */
public class EnumSerializer implements Serializer, Opcodes {

	@Override
	public void serialze(Outputable outputable, Object object,
			IdentityHashMap referenceMap) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, referenceMap);
			return;
		}

		outputable.putByte(Types.ENUM);

		Enum<?> enumVal = (Enum<?>) object;

		EnumInfo enumInfo = (EnumInfo) TransferConfig
				.getOrCreateClassInfo(enumVal.getDeclaringClass());

		BitUtils.putInt(outputable, enumInfo.getClassId());

		int enumIndex = enumInfo.toInt(enumVal);

		BitUtils.putInt(outputable, enumIndex);
	}

	@Override
	public void compile(Type type, MethodVisitor mv,
			AsmSerializerContext context) {

		mv.visitCode();
		mv.visitVarInsn(ALOAD, 2);
		Label l1 = new Label();
		mv.visitJumpInsn(IFNONNULL, l1);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_1);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
				"(B)V", true);

		mv.visitInsn(RETURN);
		mv.visitLabel(l1);

		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitIntInsn(BIPUSH, Types.ENUM);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
				"(B)V", true);

		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Enum");
		mv.visitVarInsn(ASTORE, 4);

		EnumInfo enumInfo = (EnumInfo) TransferConfig
				.getOrCreateClassInfo(TypeUtils.getRawClass(type));
		if (enumInfo != null) {

			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, enumInfo.getClassId());
			mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils",
					"putInt", "(Ltransfer/Outputable;I)V", false);

			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Enum", "ordinal",
					"()I", false);
			mv.visitVarInsn(ISTORE, 6);

		} else {

			// BitUtils.putInt2(outputable, enumInfo.getClassId());

			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Enum",
					"getDeclaringClass", "()Ljava/lang/Class;", false);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig",
					"getOrCreateClassInfo",
					"(Ljava/lang/Class;)Ltransfer/core/ClassInfo;", false);
			mv.visitTypeInsn(CHECKCAST, "transfer/core/EnumInfo");
			mv.visitVarInsn(ASTORE, 5);

			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitMethodInsn(INVOKEVIRTUAL, "transfer/core/EnumInfo",
					"getClassId", "()I", false);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils",
					"putInt", "(Ltransfer/Outputable;I)V", false);

			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "transfer/core/EnumInfo",
					"toInt", "(Ljava/lang/Enum;)I", false);
			mv.visitVarInsn(ISTORE, 6);

		}

		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ILOAD, 6);
		mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putInt",
				"(Ltransfer/Outputable;I)V", false);

		mv.visitInsn(RETURN);

		mv.visitMaxs(4, 7);
		mv.visitEnd();

	}

	private static EnumSerializer instance = new EnumSerializer();

	public static EnumSerializer getInstance() {
		return instance;
	}

}
