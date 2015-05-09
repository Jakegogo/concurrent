package transfer.serializer;

import transfer.core.SerialContext;
import utils.enhance.asm.util.AsmUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exceptions.CompileError;
import transfer.utils.BitUtils;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数字编码器 Created by Jake on 2015/2/26.
 */
public class NumberSerializer implements Serializer, Opcodes {

	// 1000 0000
	public static final int FLAG_0X80 = 0x80;

	// 0000 1000
	public static final byte FLAG_NEGATIVE = (byte) 0x08;

	// 0000 0000
	public static final byte FLAG_NOT_NEGATIVE = (byte) 0x00;

	@Override
	public void serialze(Outputable outputable, Object object,
			SerialContext context) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, context);
			return;
		}

		Number number = (Number) object;
		if (object instanceof Integer 
				|| object instanceof Short
				|| object instanceof Byte 
				|| object instanceof AtomicInteger) {
			this.putIntVal(outputable, number);
		} else if (object instanceof Long 
				|| object instanceof AtomicLong
				|| object instanceof BigInteger) {
			this.putLongVal(outputable, number);
		} else if (object instanceof Float 
				|| object instanceof Double
				|| object instanceof BigDecimal) {
			DecimalSerializer.getInstance().serialze(outputable, object,
					context);
		}

	}


	private void putIntVal(Outputable outputable, Number number) {

		int value = number.intValue();
		if (value >= 0) {
			outputable.putByte((byte) (Types.NUMBER | FLAG_NOT_NEGATIVE | TransferConfig.VARINT));
		} else {
			value = -value;
			outputable.putByte((byte) (Types.NUMBER | FLAG_NEGATIVE | TransferConfig.VARINT));
		}
		BitUtils.putInt(outputable, value);

	}


	private void putLongVal(Outputable outputable, Number number) {

		long value = number.longValue();
		if (value >= 0) {
			outputable.putByte((byte) (Types.NUMBER | FLAG_NOT_NEGATIVE | TransferConfig.VARLONG));
		} else {
			value = -value;
			outputable.putByte((byte) (Types.NUMBER | FLAG_NEGATIVE | TransferConfig.VARLONG));
		}
		BitUtils.putLong(outputable, value);

	}


	@Override
	public void compile(Type type, MethodVisitor mv,
			AsmSerializerContext context) {

		this.addPutValMethods(mv, context.getClassWriter(), context);

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

		Class<?> numberClass = TypeUtils.getRawClass(type);
		if (numberClass == null || numberClass == Object.class) {
			throw new CompileError("浮点类型不确定.(" + type + ")");
		} else if (numberClass == int.class || numberClass == Integer.class
				|| numberClass == short.class || numberClass == Short.class
				|| numberClass == byte.class || numberClass == Byte.class
				|| numberClass == AtomicInteger.class) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKESPECIAL,
					AsmUtils.toAsmCls(context.getClassName()), "putIntVal",
					"(Ltransfer/Outputable;Ljava/lang/Number;)V", false);
		} else if (numberClass == long.class || numberClass == Long.class
				|| numberClass == AtomicLong.class
				|| numberClass == BigInteger.class) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKESPECIAL,
					AsmUtils.toAsmCls(context.getClassName()), "putLongVal",
					"(Ltransfer/Outputable;Ljava/lang/Number;)V", false);
		} else {
			throw new CompileError("不支持的预编译类型:" + type);
		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(4, 5);
		mv.visitEnd();
	}

	private void addPutValMethods(MethodVisitor mv, ClassWriter cw,
			AsmSerializerContext context) {

		if (context.isAddNumberSerializeCommonMethod()) {
			return;
		}

		{
			MethodVisitor mv1 = cw.visitMethod(ACC_PRIVATE, "putIntVal",
					"(Ltransfer/Outputable;Ljava/lang/Number;)V", null, null);
			mv1.visitCode();
			Label l0 = new Label();
			mv1.visitLabel(l0);
			mv1.visitVarInsn(ALOAD, 2);
			mv1.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
			mv1.visitVarInsn(ISTORE, 3);
			Label l1 = new Label();
			mv1.visitLabel(l1);
			mv1.visitInsn(ICONST_0);
			mv1.visitVarInsn(ISTORE, 4);
			Label l2 = new Label();
			mv1.visitLabel(l2);
			mv1.visitVarInsn(ILOAD, 3);
			Label l3 = new Label();
			mv1.visitJumpInsn(IFGE, l3);
			Label l4 = new Label();
			mv1.visitLabel(l4);
			mv1.visitVarInsn(ILOAD, 3);
			mv1.visitInsn(INEG);
			mv1.visitVarInsn(ISTORE, 3);
			Label l5 = new Label();
			mv1.visitLabel(l5);
			mv1.visitIntInsn(BIPUSH, 8);
			mv1.visitVarInsn(ISTORE, 4);
			mv1.visitLabel(l3);
			mv1.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
			mv1.visitVarInsn(ALOAD, 1);
			mv1.visitIntInsn(BIPUSH, 16);
			mv1.visitVarInsn(ILOAD, 4);
			mv1.visitInsn(IOR);
			mv1.visitInsn(ICONST_0);
			mv1.visitInsn(IOR);
			mv1.visitInsn(I2B);
			mv1.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);
			Label l6 = new Label();
			mv1.visitLabel(l6);
			mv1.visitVarInsn(ALOAD, 1);
			mv1.visitVarInsn(ILOAD, 3);
			mv1.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putInt", "(Ltransfer/Outputable;I)V", false);
			Label l7 = new Label();
			mv1.visitLabel(l7);
			mv1.visitInsn(RETURN);
			Label l8 = new Label();
			mv1.visitLabel(l8);
			mv1.visitMaxs(3, 5);
			mv1.visitEnd();
		}
		{
			MethodVisitor mv1 = cw.visitMethod(ACC_PRIVATE, "putLongVal",
					"(Ltransfer/Outputable;Ljava/lang/Number;)V", null, null);
			mv1.visitCode();
			Label l0 = new Label();
			mv1.visitLabel(l0);
			mv1.visitVarInsn(ALOAD, 2);
			mv1.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
			mv1.visitVarInsn(LSTORE, 3);
			Label l1 = new Label();
			mv1.visitLabel(l1);
			mv1.visitInsn(ICONST_0);
			mv1.visitVarInsn(ISTORE, 5);
			Label l2 = new Label();
			mv1.visitLabel(l2);
			mv1.visitVarInsn(LLOAD, 3);
			mv1.visitInsn(LCONST_0);
			mv1.visitInsn(LCMP);
			Label l3 = new Label();
			mv1.visitJumpInsn(IFGE, l3);
			Label l4 = new Label();
			mv1.visitLabel(l4);
			mv1.visitVarInsn(LLOAD, 3);
			mv1.visitInsn(LNEG);
			mv1.visitVarInsn(LSTORE, 3);
			Label l5 = new Label();
			mv1.visitLabel(l5);
			mv1.visitIntInsn(BIPUSH, 8);
			mv1.visitVarInsn(ISTORE, 5);
			mv1.visitLabel(l3);
			mv1.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.LONG, Opcodes.INTEGER}, 0, null);
			mv1.visitVarInsn(ALOAD, 1);
			mv1.visitIntInsn(BIPUSH, 16);
			mv1.visitVarInsn(ILOAD, 5);
			mv1.visitInsn(IOR);
			mv1.visitInsn(ICONST_1);
			mv1.visitInsn(IOR);
			mv1.visitInsn(I2B);
			mv1.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);
			Label l6 = new Label();
			mv1.visitLabel(l6);
			mv1.visitVarInsn(ALOAD, 1);
			mv1.visitVarInsn(LLOAD, 3);
			mv1.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putLong", "(Ltransfer/Outputable;J)V", false);
			Label l7 = new Label();
			mv1.visitLabel(l7);
			mv1.visitInsn(RETURN);
			Label l8 = new Label();
			mv1.visitLabel(l8);
			mv1.visitMaxs(4, 6);
			mv1.visitEnd();
		}

		context.setAddNumberSerializeCommonMethod(true);

	}

	private static NumberSerializer instance = new NumberSerializer();

	public static NumberSerializer getInstance() {
		return instance;
	}

}
