package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.core.SerialContext;
import utils.enhance.asm.util.AsmUtils;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.ClassInfo;
import transfer.core.FieldInfo;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exceptions.CompileError;
import transfer.utils.BitUtils;
import transfer.utils.TypeUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Asm代理对象编码器 Created by Jake on 2015/2/23.
 */
public class ObjectAsmProxySerializer implements Serializer, Opcodes {

	@Override
	public void serialze(Outputable outputable, Object object,
			SerialContext context) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, context);
			return;
		}

		Class<?> clazz = object.getClass().getSuperclass();
		ClassInfo classInfo = TransferConfig.getOrCreateClassInfo(clazz);

		outputable.putByte(Types.OBJECT);
		BitUtils.putInt(outputable, classInfo.getClassId());

		for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
			Serializer fieldSerializer = TransferConfig.getSerializer(TypeUtils.getRawClass(fieldInfo.getType()));
			Object fieldValue = fieldInfo.getField(object);
			fieldSerializer.serialze(outputable, fieldValue, context);
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
		mv.visitIntInsn(BIPUSH, (int) Types.OBJECT);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
				"(B)V", true);

		Class<?> clazz = TypeUtils.getRawClass(type);
		if (clazz == Object.class || clazz.isInterface()
				|| clazz.isAnnotation()) {
			throw new CompileError("无法预编译:" + clazz.toString());
		}

		ClassInfo classInfo = TransferConfig.getOrCreateClassInfo(clazz
				.getSuperclass());

		mv.visitVarInsn(ALOAD, 1);
		mv.visitIntInsn(BIPUSH, classInfo.getClassId());
		mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putInt",
				"(Ltransfer/Outputable;I)V", false);

		Type fieldType;
		for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {

			fieldType = fieldInfo.getType();

			Class<?> fieldRawClass = TypeUtils.getRawClass(fieldType);
			if (fieldType == null || fieldType == Object.class
					|| fieldRawClass.isInterface()
					|| Modifier.isAbstract(fieldRawClass.getModifiers()) && !fieldRawClass.isArray()) {

				//  getFieldValue
				PropertyDescriptor propertyDescriptor;
				try {
					propertyDescriptor = new PropertyDescriptor(
							fieldInfo.getFieldName(), clazz);
				} catch (IntrospectionException e) {
					e.printStackTrace();
					throw new CompileError(e);
				}

				// 获取get方法
				final Method getMethod = propertyDescriptor.getReadMethod();
				final org.objectweb.asm.Type rt = org.objectweb.asm.Type
						.getReturnType(getMethod);
				final org.objectweb.asm.Type mt = org.objectweb.asm.Type
						.getType(getMethod);

				// 获取this.target
				mv.visitVarInsn(ALOAD, 4);

				mv.visitMethodInsn(INVOKEVIRTUAL,
						AsmUtils.toAsmCls(clazz.getName()), getMethod.getName(),
						mt.toString(), false);

				// 处理返回值类型 到 Object类型
				AsmUtils.withBoxingType(mv, rt);
				mv.visitVarInsn(ASTORE, 5);

				mv.visitVarInsn(ALOAD, 5);
				Label l3 = new Label();
				mv.visitJumpInsn(IFNONNULL, l3);

				mv.visitVarInsn(ALOAD, 1);
				mv.visitInsn(ICONST_1);
				mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte",
						"(B)V", true);
				Label l17 = new Label();
				mv.visitJumpInsn(GOTO, l17);
				mv.visitLabel(l3);
				

				mv.visitVarInsn(ALOAD, 5);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass",
						"()Ljava/lang/Class;", false);
				
				mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig",
						"getSerializer",
						"(Ljava/lang/reflect/Type;)Ltransfer/serializer/Serializer;",
						false);

				mv.visitVarInsn(ALOAD, 1);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 3);
				mv.visitMethodInsn(
						INVOKEINTERFACE,
						"transfer/serializer/Serializer",
						"serialze",
						"(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/core/SerialContext;)V",
						true);
				mv.visitLabel(l17);
				
			} else {

				Serializer fieldSerializer = TransferConfig.getSerializer(TypeUtils
						.getRawClass(fieldType));

				mv.visitVarInsn(ALOAD, 0);

				mv.visitVarInsn(ALOAD, 1);

				PropertyDescriptor propertyDescriptor;
				try {
					propertyDescriptor = new PropertyDescriptor(
							fieldInfo.getFieldName(), clazz);
				} catch (IntrospectionException e) {
					e.printStackTrace();
					throw new CompileError(e);
				}

				// 获取get方法
				final Method getMethod = propertyDescriptor.getReadMethod();
				final org.objectweb.asm.Type rt = org.objectweb.asm.Type
						.getReturnType(getMethod);
				final org.objectweb.asm.Type mt = org.objectweb.asm.Type
						.getType(getMethod);

				// 获取this.target
				mv.visitVarInsn(ALOAD, 2);

				mv.visitMethodInsn(INVOKEVIRTUAL,
						AsmUtils.toAsmCls(clazz.getName()), getMethod.getName(),
						mt.toString(), false);

				// 处理返回值类型 到 Object类型
				AsmUtils.withBoxingType(mv, rt);

				mv.visitVarInsn(ALOAD, 3);

				// 执行属性预编译
				MethodVisitor methodVisitor = context.invokeNextSerialize(
						fieldInfo.getFieldName(), mv);

				fieldSerializer
						.compile(fieldInfo.getType(), methodVisitor, context);
			}

		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(2, 4);
		mv.visitEnd();

	}

	private static final ObjectAsmProxySerializer instance = new ObjectAsmProxySerializer();

	public static ObjectAsmProxySerializer getInstance() {
		return instance;
	}
}
