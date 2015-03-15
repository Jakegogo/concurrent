package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.IdentityHashMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;

/**
 * 数组编码器 Created by Jake on 2015/2/25.
 */
public class ArraySerializer implements Serializer, Opcodes {

	@Override
	public void serialze(Outputable outputable, Object object,
			IdentityHashMap referenceMap) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, object, referenceMap);
			return;
		}

		outputable.putByte(Types.ARRAY);

		Object[] array = (Object[]) object;

		for (Object obj : array) {

			Serializer elementSerializer = TransferConfig.getSerializer(obj
					.getClass());

			elementSerializer.serialze(outputable, obj, referenceMap);
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

		mv.visitVarInsn(ALOAD, 1);
		mv.visitIntInsn(BIPUSH, (int) Types.ARRAY);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
				"(B)V", true);

		Class<?> componentClass = TypeUtils.getRawClass(type)
				.getComponentType();

		if (componentClass == null || componentClass == Object.class) {

			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
			mv.visitVarInsn(ASTORE, 4);

			mv.visitVarInsn(ALOAD, 4);
			mv.visitInsn(ARRAYLENGTH);
			mv.visitVarInsn(ISTORE, 7);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 6);
			Label l6 = new Label();
			mv.visitJumpInsn(GOTO, l6);
			Label l7 = new Label();
			mv.visitLabel(l7);

			mv.visitFrame(Opcodes.F_FULL, 9, new Object[] {
					"transfer/serializer/ArraySerializer",
					"transfer/Outputable", "java/lang/Object",
					"transfer/utils/IdentityHashMap", "[Ljava/lang/Object;",
					Opcodes.TOP, Opcodes.INTEGER, Opcodes.INTEGER,
					"[Ljava/lang/Object;" }, 0, new Object[] {});
			mv.visitVarInsn(ALOAD, 4);
			mv.visitVarInsn(ILOAD, 6);
			mv.visitInsn(AALOAD);
			mv.visitVarInsn(ASTORE, 5);

			mv.visitVarInsn(ALOAD, 5);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass",
					"()Ljava/lang/Class;", false);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig",
					"getSerializer",
					"(Ljava/lang/Class;)Ltransfer/serializer/Serializer;",
					false);
			mv.visitVarInsn(ASTORE, 9);

			mv.visitVarInsn(ALOAD, 9);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(
					INVOKEINTERFACE,
					"transfer/serializer/Serializer",
					"serialze",
					"(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/utils/IdentityHashMap;)V",
					true);


			mv.visitLabel(l6);
			mv.visitIincInsn(6, 1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ILOAD, 6);
			mv.visitVarInsn(ILOAD, 7);
			mv.visitJumpInsn(IF_ICMPLT, l7);

		} else {

			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
			mv.visitVarInsn(ASTORE, 4);

			mv.visitVarInsn(ALOAD, 4);

			mv.visitInsn(ARRAYLENGTH);
			mv.visitVarInsn(ISTORE, 7);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 6);
			Label l6 = new Label();
			mv.visitJumpInsn(GOTO, l6);
			Label l7 = new Label();
			mv.visitLabel(l7);

			mv.visitFrame(Opcodes.F_FULL, 9, new Object[]{
					"transfer/serializer/ArraySerializer",
					"transfer/Outputable", "java/lang/Object",
					"transfer/utils/IdentityHashMap", "[Ljava/lang/Object;",
					Opcodes.TOP, Opcodes.INTEGER, Opcodes.INTEGER,
					"[Ljava/lang/Object;" }, 0, new Object[] {});
			mv.visitVarInsn(ALOAD, 4);
			mv.visitVarInsn(ILOAD, 6);
			mv.visitInsn(AALOAD);
			mv.visitVarInsn(ASTORE, 8);

			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 8);
			mv.visitVarInsn(ALOAD, 3);

			// 执行属性预编译
			MethodVisitor methodVisitor = context.invokeNextSerialize(null, mv);

			Serializer fieldSerializer = TransferConfig
					.getSerializer(componentClass);
			fieldSerializer.compile(componentClass, methodVisitor, context);

			mv.visitLabel(l6);
			mv.visitIincInsn(6, 1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ILOAD, 6);
			mv.visitVarInsn(ILOAD, 7);
			mv.visitJumpInsn(IF_ICMPLT, l7);

		}

		mv.visitInsn(RETURN);

		mv.visitMaxs(4, 10);
		mv.visitEnd();

	}

	private static ArraySerializer instance = new ArraySerializer();

	public static ArraySerializer getInstance() {
		return instance;
	}

}
