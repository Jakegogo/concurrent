package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.SerialContext;
import transfer.def.Types;

import java.lang.reflect.Type;

/**
 * NULL编码器 Created by Jake on 2015/2/26.
 */
public class NullSerializer implements Serializer, Opcodes {

	@Override
	public void serialze(Outputable outputable, Object object,
			SerialContext context) {

		outputable.putByte(Types.NULL);

	}

	@Override
	public void compile(Type type, MethodVisitor mv,
			AsmSerializerContext context) {

		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_1);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
				"(B)V", true);
		mv.visitInsn(RETURN);
		mv.visitMaxs(2, 4);
		mv.visitEnd();

	}

	private static NullSerializer instance = new NullSerializer();

	public static NullSerializer getInstance() {
		return instance;
	}

}
