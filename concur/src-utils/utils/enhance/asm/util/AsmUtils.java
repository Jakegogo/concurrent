package utils.enhance.asm.util;

import org.objectweb.asm.*;

import utils.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 类操作的工具集
 * @author Jake
 * @date 2014年9月6日上午12:10:37
 */
public class AsmUtils implements Opcodes {

	
	/**
	 * 是否为基本类型的包装类
	 * @param clz 目标类型
	 * @return
	 */
	public static boolean isWrapClass(Class<?> clz) {
		if(clz.isPrimitive()) {
			return false;
		}
        try { 
        	return ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) { 
        	return false; 
        } 
    }
	
	
	/**
	 * 是否基本类型(包括包装类型)
	 * @param clzz 目标类型
	 * @return
	 */
	public static boolean isBaseType(Class<?> clzz) {
		return clzz.isPrimitive() || clzz == Object.class || isWrapClass(clzz);
	}
	
	
	/**
	 * 获取get方法名
	 * @param field 属性
	 * @return
	 */
	public static String getGetterMethodName(Field field) {
		if(field.getType() == boolean.class) {
			return "is" + StringUtils.getUString(field.getName());
		}
		return "get" + StringUtils.getUString(field.getName());
	}
	
	
	/**
	 * 获取set方法名
	 * @param field 属性
	 * @return
	 */
	public static String getSetterMethodName(Field field) {
		return "set" + StringUtils.getUString(field.getName());
	}
	

	/**
	 * 把类名中的"."替换为"/"
	 *
	 * @param className
	 * @return
	 */
	public static String toAsmCls(String className) {
		return className.replace('.', '/');
	}


	/**
	 * 判断是否需要重写方法
	 * <p>
	 * object类本身的方法不做重写
	 * </p>
	 * <p>
	 * "main" 方法不做重写
	 * </p>
	 *
	 * @param m
	 *            目标方法
	 * @return
	 */
	public static boolean needOverride(Method m) {
		// object类本身的方法不做重写
		if (m.getDeclaringClass().getName().equals(Object.class.getName())) {
			return false;
		}
		// 复合方法
		if (m.isSynthetic()) {
			return false;
		}
		// "main" 方法不做重写
		return !(Modifier.isPublic(m.getModifiers())
				&& Modifier.isStatic(m.getModifiers())
				&& m.getReturnType().getName().equals("void")
				&& m.getName().equals("main"));
	}


	public static String getPrimitiveLetter(Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        } else if (Void.TYPE.equals(type)) {
            return "V";
        } else if (Boolean.TYPE.equals(type)) {
            return "Z";
        } else if (Character.TYPE.equals(type)) {
            return "C";
        } else if (Byte.TYPE.equals(type)) {
            return "B";
        } else if (Short.TYPE.equals(type)) {
            return "S";
        } else if (Float.TYPE.equals(type)) {
            return "F";
        } else if (Long.TYPE.equals(type)) {
            return "J";
        } else if (Double.TYPE.equals(type)) {
            return "D";
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }


    public static java.lang.reflect.Type getMethodType(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName);

            return method.getGenericReturnType();
        } catch (Exception ex) {
            return null;
        }
    }


    public static java.lang.reflect.Type getFieldType(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);

            return field.getGenericType();
        } catch (Exception ex) {
            return null;
        }
    }



	/**
	 *
	 * <p>
	 * get StoreCode(Opcodes#xStore)
	 * </p>
	 *
	 *
	 * @param type
	 * @return
	 */
	public static int storeCode(Type type) {

		int sort = type.getSort();
		switch (sort) {
		case Type.ARRAY:
			sort = ASTORE;
			break;
		case Type.BOOLEAN:
			sort = ISTORE;
			break;
		case Type.BYTE:
			sort = ISTORE;
			break;
		case Type.CHAR:
			sort = ISTORE;
			break;
		case Type.DOUBLE:
			sort = DSTORE;
			break;
		case Type.FLOAT:
			sort = FSTORE;
			break;
		case Type.INT:
			sort = ISTORE;
			break;
		case Type.LONG:
			sort = LSTORE;
			break;
		case Type.OBJECT:
			sort = ASTORE;
			break;
		case Type.SHORT:
			sort = ISTORE;
			break;
		default:
			break;
		}
		return sort;
	}


	/**
	 *
	 * <p>
	 * get StoreCode(Opcodes#xLOAD)
	 * </p>
	 *
	 * @param type
	 * @return
	 */
	public static int loadCode(Type type) {
		int sort = type.getSort();
		switch (sort) {
		case Type.ARRAY:
			sort = ALOAD;
			break;
		case Type.BOOLEAN:
			sort = ILOAD;
			break;
		case Type.BYTE:
			sort = ILOAD;
			break;
		case Type.CHAR:
			sort = ILOAD;
			break;
		case Type.DOUBLE:
			sort = DLOAD;
			break;
		case Type.FLOAT:
			sort = FLOAD;
			break;
		case Type.INT:
			sort = ILOAD;
			break;
		case Type.LONG:
			sort = LLOAD;
			break;
		case Type.OBJECT:
			sort = ALOAD;
			break;
		case Type.SHORT:
			sort = ILOAD;
			break;
		default:
			break;
		}
		return sort;
	}


	/**
	 *
	 * <p>
	 * get StoreCode(Opcodes#xRETURN)
	 * </p>
	 *
	 * @param type
	 * @return
	 */
	public static int rtCode(Type type) {
		int sort = type.getSort();
		switch (sort) {
		case Type.ARRAY:
			sort = ARETURN;
			break;
		case Type.BOOLEAN:
			sort = IRETURN;
			break;
		case Type.BYTE:
			sort = IRETURN;
			break;
		case Type.CHAR:
			sort = IRETURN;
			break;
		case Type.DOUBLE:
			sort = DRETURN;
			break;
		case Type.FLOAT:
			sort = FRETURN;
			break;
		case Type.INT:
			sort = IRETURN;
			break;
		case Type.LONG:
			sort = LRETURN;
			break;
		case Type.OBJECT:
			sort = ARETURN;
			break;
		case Type.SHORT:
			sort = IRETURN;
			break;
		default:
			break;
		}
		return sort;
	}


	/**
	 * 封装成包装类型
	 */
	public static void withBoxingType(MethodVisitor mWriter, Type fieldType) {
		switch (fieldType.getSort()) {
			case Type.VOID:
				break;
			case Type.BOOLEAN:
				mWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
				break;
			case Type.BYTE:
				mWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
				break;
			case Type.CHAR:
				mWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
				break;
			case Type.SHORT:
				mWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
				break;
			case Type.INT:
				mWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
				break;
			case Type.FLOAT:
				mWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
				break;
			case Type.LONG:
				mWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
				break;
			case Type.DOUBLE:
				mWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
				break;
		}
	}
	
	
	/**
	 * 转换成拆箱类型
	 * @param mWriter
	 * @param fieldType
	 */
	public static void withUnBoxingType(MethodVisitor mWriter, Type fieldType) {
		if(fieldType == Type.VOID_TYPE) {
		} else if(fieldType == Type.INT_TYPE) {
			mWriter.visitTypeInsn(CHECKCAST, "java/lang/Integer");
			mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
		} else if(fieldType == Type.BOOLEAN_TYPE) {
			mWriter.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
			mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
		} else if(fieldType == Type.BYTE_TYPE) {
			mWriter.visitTypeInsn(CHECKCAST, "java/lang/Byte");
			mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
		} else if(fieldType == Type.CHAR_TYPE) {
			mWriter.visitTypeInsn(CHECKCAST, "java/lang/Character");
			mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
		} else if(fieldType == Type.SHORT_TYPE) {
			mWriter.visitTypeInsn(CHECKCAST, "java/lang/Short");
			mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
		} else if(fieldType == Type.FLOAT_TYPE) {
			mWriter.visitTypeInsn(CHECKCAST, "java/lang/Float");
			mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
		} else if(fieldType == Type.LONG_TYPE) {
			mWriter.visitTypeInsn(CHECKCAST, "java/lang/Long");
			mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
		} else if(fieldType == Type.DOUBLE_TYPE) {
			mWriter.visitTypeInsn(CHECKCAST, "java/lang/Double");
			mWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
		}
	}
	
	
	/**
	 * 获取加载数组元素指令
	 * @param type 数组元素类型
	 * @return
	 */
	public static int loadArrayCode(org.objectweb.asm.Type type) {
		switch (type.getSort()) {
			case Type.OBJECT:
				return AALOAD;
			case Type.BOOLEAN:
				return BALOAD;
			case Type.CHAR:
				return CALOAD;
			case Type.SHORT:
				return SALOAD;
			case Type.INT:
				return IALOAD;
			case Type.FLOAT:
				return FALOAD;
			case Type.LONG:
				return LALOAD;
			case Type.DOUBLE:
				return DALOAD;
		}
		
		return AALOAD;
	}
	
	
	/**
	 * 获取存储数组元素指令
	 * @param type 数组元素类型
	 * @return
	 */
	public static int storeArrayCode(org.objectweb.asm.Type type) {
		switch (type.getSort()) {
			case Type.OBJECT:
				return AASTORE;
			case Type.BOOLEAN:
				return BASTORE;
			case Type.CHAR:
				return CASTORE;
			case Type.SHORT:
				return SASTORE;
			case Type.INT:
				return IASTORE;
			case Type.FLOAT:
				return FASTORE;
			case Type.LONG:
				return LASTORE;
			case Type.DOUBLE:
				return DASTORE;
		}
		return AALOAD;
	}
	
	
	/**
	 * 创建数组指令
	 * @param componentClass 数组元素类型
	 * @return
	 */
	public static int newArrayCode(Class<?> type) {
		if (Integer.TYPE.equals(type)) {
            return T_INT;
        } else if (Boolean.TYPE.equals(type)) {
            return T_BOOLEAN;
        } else if (Character.TYPE.equals(type)) {
            return T_CHAR;
        } else if (Byte.TYPE.equals(type)) {
            return T_BYTE;
        } else if (Short.TYPE.equals(type)) {
            return T_SHORT;
        } else if (Float.TYPE.equals(type)) {
            return T_FLOAT;
        } else if (Long.TYPE.equals(type)) {
            return T_LONG;
        } else if (Double.TYPE.equals(type)) {
            return T_DOUBLE;
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
	}
	
	
	/**
	 * 获取基本类型的TYPE
	 * @param mv 
	 * @param fieldRawClass 类型
	 */
	public static void loadPrimitiveType(MethodVisitor mv, Class<?> fieldRawClass) {
		
		if (fieldRawClass == int.class || fieldRawClass == Integer.class) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
		} else if (fieldRawClass == short.class || fieldRawClass == Short.class) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
		} else if (fieldRawClass == long.class || fieldRawClass == Long.class) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
		} else if (fieldRawClass == float.class || fieldRawClass == Float.class) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
		} else if (fieldRawClass == double.class || fieldRawClass == Double.class) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
		} else if (fieldRawClass == byte.class || fieldRawClass == Byte.class) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
		} else if (fieldRawClass == boolean.class || fieldRawClass == Boolean.class) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
		} else if (fieldRawClass == char.class || fieldRawClass == Character.class) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
		}
		
	}
	
	
	/**
	 *
	 * <p>
	 * 比较参数类型是否一致
	 * </p>
	 *
	 * @param types
	 *            asm的类型({@link Type})
	 * @param clazzes
	 *            java 类型({@link Class})
	 * @return
	 */
	private static boolean sameType(Type[] types, Class<?>[] clazzes) {
		// 个数不同
		if (types.length != clazzes.length) {
			return false;
		}

		for (int i = 0; i < types.length; i++) {
			if (!Type.getType(clazzes[i]).equals(types[i])) {
				return false;
			}
		}

		return true;
	}


	/**
	 *
	 * <p>
	 * 获取方法的参数名
	 * </p>
	 * 兼容JDK1.6
	 * @param m
	 * @return
	 */
	public static Map<String, Class<?>> getMethodParams(final Method m) {
		//获取参数类型列表
		final Class<?>[] parameterTypes = m.getParameterTypes();

		final Map<String, Class<?>> paramMap = new LinkedHashMap<String, Class<?>>(parameterTypes.length);

		final String n = m.getDeclaringClass().getName();
		ClassReader cr;

		try {
			cr = new ClassReader(n);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		cr.accept(new ClassVisitor(Opcodes.ASM4) {

			@Override
			public MethodVisitor visitMethod(final int access,
					final String name, final String desc,
					final String signature, final String[] exceptions) {
				final Type[] args = Type.getArgumentTypes(desc);
				// 方法名相同并且参数个数相同
				if (!name.equals(m.getName())
						|| !sameType(args, m.getParameterTypes())) {
					return super.visitMethod(access, name, desc, signature,
							exceptions);
				}
				MethodVisitor v = super.visitMethod(access, name, desc,
						signature, exceptions);

				return new MethodVisitor(Opcodes.ASM4, v) {
					@Override
					public void visitLocalVariable(String name, String desc,
							String signature, Label start, Label end, int index) {
						int i = index - 1;
						// 如果是静态方法，则第一就是参数
						// 如果不是静态方法，则第一个是"this"，然后才是方法的参数
						if (Modifier.isStatic(m.getModifiers())) {
							i = index;
						}
						if (i >= 0 && i < parameterTypes.length) {
							paramMap.put(name, parameterTypes[i]);
						}
						super.visitLocalVariable(name, desc, signature, start,
								end, index);
					}

				};

			}
		}, 0);

		return paramMap;
	}



	/**
	 *
	 * <p>
	 * 把java字节码写入class文件
	 * </p>
	 *
	 * @param <T>
	 * @param name
	 * @param data
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static <T> void writeClazz(String name, byte[] data) {
		try {
			File file = new File("C:\\" + name + ".class");
			FileOutputStream fout = new FileOutputStream(file);

			fout.write(data);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
