package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map编码器 Created by Jake on 2015/2/25.
 */
public class MapSerializer implements Serializer, Opcodes {

	@Override
	public void serialze(Outputable outputable, Object object,
			IdentityHashMap referenceMap) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, object, referenceMap);
			return;
		}

		outputable.putByte(Types.MAP);

		Map<?, ?> map = (Map<?, ?>) object;

		// 设置Map的大小
		BitUtils.putInt(outputable, map.size());

		Object key, value;
		for (Map.Entry entry : map.entrySet()) {

			key = entry.getKey();

			Serializer keySerializer = TransferConfig.getSerializer(key
					.getClass());

			keySerializer.serialze(outputable, key, referenceMap);

			value = entry.getValue();

			Serializer valueSerializer = TransferConfig.getSerializer(value
					.getClass());

			valueSerializer.serialze(outputable, value, referenceMap);
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
		mv.visitIntInsn(BIPUSH, Types.MAP);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
				"(B)V", true);

		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(CHECKCAST, "java/util/Map");
		mv.visitVarInsn(ASTORE, 4);

		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 4);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "size", "()I",
				true);
		mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putInt",
				"(Ltransfer/Outputable;I)V", false);

		Class<?> keyClass = TypeUtils.getParameterizedClass(type, 0);
		Class<?> valueClass = TypeUtils.getParameterizedClass(type, 1);

		mv.visitVarInsn(ALOAD, 4);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "entrySet",
				"()Ljava/util/Set;", true);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator",
				"()Ljava/util/Iterator;", true);
		mv.visitVarInsn(ASTORE, 8);
		Label l7 = new Label();
		mv.visitJumpInsn(GOTO, l7);
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitFrame(Opcodes.F_FULL, 9, new Object[] {
				"transfer/serializer/MapSerializer", "transfer/Outputable",
				"java/lang/Object", "transfer/utils/IdentityHashMap",
				"java/util/Map", Opcodes.TOP, Opcodes.TOP, Opcodes.TOP,
				"java/util/Iterator" }, 0, new Object[] {});
		mv.visitVarInsn(ALOAD, 8);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next",
				"()Ljava/lang/Object;", true);
		mv.visitTypeInsn(CHECKCAST, "java/util/Map$Entry");
		mv.visitVarInsn(ASTORE, 7);

		mv.visitVarInsn(ALOAD, 7);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey",
				"()Ljava/lang/Object;", true);
		mv.visitVarInsn(ASTORE, 5);

		int localNumAppend = 0;

		if ((keyClass == null || keyClass == Object.class)) {

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

		} else {

			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 3);

			// 执行属性预编译
			MethodVisitor methodVisitor = context.invokeNextSerialize(null, mv);

			Serializer fieldSerializer = TransferConfig.getSerializer(keyClass);

			Type keyType = TypeUtils.getParameterizedType(
					(ParameterizedType) type, 0);

			fieldSerializer.compile(keyType, methodVisitor, context);

			localNumAppend -= 1;
		}

		mv.visitVarInsn(ALOAD, 7);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getValue",
				"()Ljava/lang/Object;", true);
		mv.visitVarInsn(ASTORE, 6);

		if (valueClass == null || valueClass == Object.class) {

			mv.visitVarInsn(ALOAD, 6);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass",
					"()Ljava/lang/Class;", false);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig",
					"getSerializer",
					"(Ljava/lang/Class;)Ltransfer/serializer/Serializer;",
					false);
			mv.visitVarInsn(ASTORE, 10 + localNumAppend);

			mv.visitVarInsn(ALOAD, 10 + localNumAppend);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(
					INVOKEINTERFACE,
					"transfer/serializer/Serializer",
					"serialze",
					"(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/utils/IdentityHashMap;)V",
					true);

		} else {

			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitVarInsn(ALOAD, 3);

			// 执行属性预编译
			MethodVisitor methodVisitor = context.invokeNextSerialize(null, mv);

			Serializer fieldSerializer = TransferConfig
					.getSerializer(valueClass);

			Type valueType = TypeUtils.getParameterizedType(
					(ParameterizedType) type, 1);

			fieldSerializer.compile(valueType, methodVisitor, context);

			localNumAppend -= 1;
		}

		mv.visitLabel(l7);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext",
				"()Z", true);
		mv.visitJumpInsn(IFNE, l8);

		mv.visitInsn(RETURN);
		mv.visitMaxs(4, 11);
		mv.visitEnd();

	}

	private static MapSerializer instance = new MapSerializer();

	public static MapSerializer getInstance() {
		return instance;
	}

}
