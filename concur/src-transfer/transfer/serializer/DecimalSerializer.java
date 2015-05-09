package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.SerialContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exceptions.CompileError;
import transfer.utils.BitUtils;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * 浮点数编码器 Created by Jake on 2015/2/26.
 */
public class DecimalSerializer implements Serializer, Opcodes {

	@Override
	public void serialze(Outputable outputable, Object object,
			SerialContext context) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, context);
			return;
		}

		Number number = (Number) object;
		if (object instanceof Float) {
			outputable.putByte((byte) (Types.DECIMAL | TransferConfig.FLOAT));
			BitUtils.putInt(outputable, Float.floatToRawIntBits(number.floatValue()));
		} else if (object instanceof Double || object instanceof BigDecimal) {
			outputable.putByte((byte) (Types.DECIMAL | TransferConfig.DOUBLE));
			BitUtils.putLong(outputable, Double.doubleToRawLongBits(number.doubleValue()));
		}

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

		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
		mv.visitVarInsn(ASTORE, 4);

		Class<?> decimalClass = TypeUtils.getRawClass(type);
		if (decimalClass == null || decimalClass == Object.class) {
			throw new CompileError("浮点类型不确定.(" + type + ")");
		} else if (decimalClass == float.class || decimalClass == Float.class) {

			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(BIPUSH, (Types.DECIMAL | TransferConfig.FLOAT));
			mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable",
					"putByte", "(B)V", true);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "floatValue",
					"()F", false);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float",
					"floatToRawIntBits", "(F)I", false);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils",
					"putInt", "(Ltransfer/Outputable;I)V", false);

		} else if (decimalClass == double.class || decimalClass == Double.class) {

			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(BIPUSH, (Types.DECIMAL | TransferConfig.DOUBLE));
			mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable",
					"putByte", "(B)V", true);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number",
					"doubleValue", "()D", false);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double",
					"doubleToRawLongBits", "(D)J", false);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils",
					"putLong", "(Ltransfer/Outputable;J)V", false);

		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(4, 5);
		mv.visitEnd();

	}

	private static DecimalSerializer instance = new DecimalSerializer();

	public static DecimalSerializer getInstance() {
		return instance;
	}

}
