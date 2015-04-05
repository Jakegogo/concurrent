package transfer.serializer;

import dbcache.support.asm.util.AsmUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.CompileError;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;
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
			IdentityHashMap referenceMap) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, referenceMap);
			return;
		}

		Number number = (Number) object;

		if (object instanceof Integer || object instanceof Short
				|| object instanceof Byte || object instanceof AtomicInteger) {

			this.putIntVal(outputable, number);

		} else if (object instanceof Long || object instanceof AtomicLong
				|| object instanceof BigInteger) {

			this.putLongVal(outputable, number);

		} else if (object instanceof Float || object instanceof Double
				|| object instanceof BigDecimal) {

			DecimalSerializer.getInstance().serialze(outputable, object,
					referenceMap);
		}

	}

	private void putIntVal(Outputable outputable, Number number) {

		int value = number.intValue();

		byte sign = FLAG_NOT_NEGATIVE;
		if (value < 0) {
			value = -value;
			sign = FLAG_NEGATIVE;
		}

		outputable.putByte((byte) (Types.NUMBER | sign | TransferConfig.VARINT));
		BitUtils.putInt(outputable, value);

	}

	private void putLongVal(Outputable outputable, Number number) {

		long value = number.longValue();

		byte sign = FLAG_NOT_NEGATIVE;
		if (value < 0) {
			value = -value;
			sign = FLAG_NEGATIVE;
		}

		outputable.putByte((byte) (Types.NUMBER | sign | TransferConfig.VARLONG));
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
			mv = cw.visitMethod(ACC_PRIVATE, "putIntVal",
					"(Ltransfer/Outputable;Ljava/lang/Number;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
			mv.visitVarInsn(ISTORE, 3);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 4);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitVarInsn(ILOAD, 3);
			Label l3 = new Label();
			mv.visitJumpInsn(IFGE, l3);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitInsn(INEG);
			mv.visitVarInsn(ISTORE, 3);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitIntInsn(BIPUSH, 8);
			mv.visitVarInsn(ISTORE, 4);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(BIPUSH, 16);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitInsn(IOR);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(IOR);
			mv.visitInsn(I2B);
			mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putInt", "(Ltransfer/Outputable;I)V", false);
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitInsn(RETURN);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitMaxs(3, 5);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE, "putLongVal",
					"(Ltransfer/Outputable;Ljava/lang/Number;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
			mv.visitVarInsn(LSTORE, 3);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 5);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitVarInsn(LLOAD, 3);
			mv.visitInsn(LCONST_0);
			mv.visitInsn(LCMP);
			Label l3 = new Label();
			mv.visitJumpInsn(IFGE, l3);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitVarInsn(LLOAD, 3);
			mv.visitInsn(LNEG);
			mv.visitVarInsn(LSTORE, 3);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitIntInsn(BIPUSH, 8);
			mv.visitVarInsn(ISTORE, 5);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.LONG, Opcodes.INTEGER}, 0, null);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(BIPUSH, 16);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(IOR);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IOR);
			mv.visitInsn(I2B);
			mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(LLOAD, 3);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putLong", "(Ltransfer/Outputable;J)V", false);
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitInsn(RETURN);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitMaxs(4, 6);
			mv.visitEnd();
		}

		context.setAddNumberSerializeCommonMethod(true);

	}

	private static NumberSerializer instance = new NumberSerializer();

	public static NumberSerializer getInstance() {
		return instance;
	}

}
