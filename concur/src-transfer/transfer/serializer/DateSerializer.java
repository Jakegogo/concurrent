package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.SerialContext;
import transfer.def.Types;
import transfer.utils.BitUtils;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * 日期编码器 Created by Jake on 2015/2/26.
 */
public class DateSerializer implements Serializer, Opcodes {

	@Override
	public void serialze(Outputable outputable, Object object,
			SerialContext context) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, context);
			return;
		}

		outputable.putByte(Types.DATE_TIME);
		Date date = (Date) object;
		BitUtils.putLong(outputable, date.getTime());
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
		mv.visitIntInsn(BIPUSH, (int) Types.DATE_TIME);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
				"(B)V", true);

		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(CHECKCAST, "java/util/Date");
		mv.visitVarInsn(ASTORE, 4);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 4);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Date", "getTime", "()J",
				false);
		mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putLong",
				"(Ltransfer/Outputable;J)V", false);

		mv.visitInsn(RETURN);

		mv.visitMaxs(4, 5);
		mv.visitEnd();

	}

	private static DateSerializer instance = new DateSerializer();

	public static DateSerializer getInstance() {
		return instance;
	}

}
