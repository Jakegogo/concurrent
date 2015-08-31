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
import java.nio.charset.Charset;

/**
 * 短字符串编码器 最大长度255 Created by Jake on 2015/2/26.
 */
public class ShortStringSerializer implements Serializer, Opcodes {

	public void serialze(Outputable outputable, Object object,
			SerialContext context) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, context);
			return;
		}

		outputable.putByte(Types.STRING);
		CharSequence charSequence = (CharSequence) object;
		String string = charSequence.toString();

		byte[] bytes = string.getBytes(Charset.forName("UTF-8"));
		BitUtils.putInt(outputable, bytes.length);
		outputable.putBytes(bytes);
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
		mv.visitIntInsn(BIPUSH, Types.STRING);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
				"(B)V", true);

		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(CHECKCAST, "java/lang/CharSequence");
		mv.visitVarInsn(ASTORE, 4);

		mv.visitVarInsn(ALOAD, 4);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/CharSequence",
				"toString", "()Ljava/lang/String;", true);
		mv.visitVarInsn(ASTORE, 5);

		mv.visitVarInsn(ALOAD, 5);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes",
				"()[B", false);
		mv.visitVarInsn(ASTORE, 6);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putInt",
				"(Ltransfer/Outputable;I)V", false);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putBytes",
				"([B)V", true);

		mv.visitInsn(RETURN);

		mv.visitMaxs(4, 7);
		mv.visitEnd();

	}

	private static final ShortStringSerializer instance = new ShortStringSerializer();

	public static ShortStringSerializer getInstance() {
		return instance;
	}

}
