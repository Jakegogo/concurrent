package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.SerialContext;
import transfer.def.TransferConfig;
import transfer.exceptions.CompileError;
import transfer.utils.TypeUtils;
import utils.enhance.asm.util.AsmUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数字编码器 Created by Jake on 2015/2/26.
 */
public class NumberSerializer implements Serializer, Opcodes {


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
			TransferConfig.putIntVal(outputable, number);
		} else if (object instanceof Long 
				|| object instanceof AtomicLong
				|| object instanceof BigInteger) {
			TransferConfig.putLongVal(outputable, number);
		} else if (object instanceof Float 
				|| object instanceof Double
				|| object instanceof BigDecimal) {
			DecimalSerializer.getInstance().serialze(outputable, object,
					context);
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

		Class<?> numberClass = TypeUtils.getRawClass(type);
		if (numberClass == null || numberClass == Object.class) {
			throw new CompileError("浮点类型不确定.(" + type + ")");
		} else if (numberClass == int.class || numberClass == Integer.class
				|| numberClass == short.class || numberClass == Short.class
				|| numberClass == byte.class || numberClass == Byte.class
				|| numberClass == AtomicInteger.class) {
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC,
					AsmUtils.toAsmCls(TransferConfig.class.getName()), "putIntVal",
					"(Ltransfer/Outputable;Ljava/lang/Number;)V", false);
		} else if (numberClass == long.class || numberClass == Long.class
				|| numberClass == AtomicLong.class
				|| numberClass == BigInteger.class) {
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC,
					AsmUtils.toAsmCls(TransferConfig.class.getName()), "putLongVal",
					"(Ltransfer/Outputable;Ljava/lang/Number;)V", false);
		} else {
			throw new CompileError("不支持的预编译类型:" + type);
		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(4, 5);
		mv.visitEnd();
	}


	private static final NumberSerializer instance = new NumberSerializer();

	public static NumberSerializer getInstance() {
		return instance;
	}

}
