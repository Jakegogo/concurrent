package utils.enhance.asm;

import dbcache.EnhanceAccessError;
import utils.enhance.asm.util.AsmUtils;
import utils.enhance.asm.util.ReflectUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * ASM读取object属性工具类
 * @author Jake
 * @date 2014年11月5日上午1:05:14
 */
public class AsmAccessHelper implements Opcodes {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(AsmAccessHelper.class);

	/** 获取的方法名 */
	public static final String GETTER_METHOD_NAME = "get";

	/** 设置的方法名 */
	public static final String SETTER_METHOD_NAME = "set";

	/** 获取值名称 */
	public static final String GET_NAME_METHOD_NAME = "getName";

	/** 构造方法名常量 */
	private static final String INIT = "<init>";

	/** 代理类类名 */
	public static final String SUFIX = "$EnhancedByAsmFieldGetter";

	/** 代理类类名 */
	public static final String SUFIX1 = "$EnhancedByAsmFieldSetter";

	/** 分隔符 */
	public static final String SPLITER = "_";

	/** 序号生成器 */
	private static AtomicLong id = new AtomicLong(0);

	/**
	 * 字节码类加载器
	 */
	public static final AsmClassLoader classLoader = new AsmClassLoader();
	
	// innder caches
	
	// putFieldsMthodMap缓存
	private static Map<Class<?>, Map<Method, List<String>>> putFieldsMthodMapCache = new ConcurrentHashMap<Class<?>, Map<Method, List<String>>>();
	
	
	/**
	 * 创建属性获取器
	 * @param getterName 名称
	 * @param clazz 类
	 * @param field 属性
	 * @return ValueGetter<T>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> ValueGetter<T> createFieldGetter(final String getterName, final Class<T> clazz, final Field field) throws Exception {
		Class<T> enhancedClass;
		//代理类名
		final String enhancedClassName = AbstractFieldGetter.class.getName()
				+ SUFIX + SPLITER + clazz.getSimpleName() + SPLITER
				+ id.incrementAndGet();
		try {
			enhancedClass = (Class<T>) classLoader.loadClass(enhancedClassName);
		} catch (ClassNotFoundException classNotFoundException) {
			ClassReader reader = null;
			try {
				reader = new ClassReader(AbstractFieldGetter.class.getName());
			} catch (IOException ioexception) {
				throw new RuntimeException(ioexception);
			}


			PropertyDescriptor propertyDescriptor = null;
			try {
				propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
			} catch (IntrospectionException e) {
				logger.error("无法获取get方法:" + clazz.getName() + "(" + field.getName() + ")");
				e.printStackTrace();
				throw new EnhanceAccessError("无法获取get方法:" + clazz.getName() + "(" + field.getName() + ")", e);
			}
			//获取get方法
			final Method getMethod = propertyDescriptor.getReadMethod();
			if (getMethod == null)  {
				logger.error("无法获取get方法:" + clazz.getName() + "(" + field.getName() + ")");
				throw new EnhanceAccessError("无法获取get方法:" + clazz.getName() + "(" + field.getName() + ")", null);
			}
			final Type mt = Type.getType(getMethod);
			final Type rt = Type.getReturnType(getMethod);

			final String fieldGetterClassName = AsmUtils.toAsmCls(AbstractFieldGetter.class.getName());
			final String entityTypeString = Type.getDescriptor(clazz);
			final String fieldGetterTypeString = Type.getDescriptor(AbstractFieldGetter.class);
			final String fieldGetterTypeString1 = fieldGetterTypeString.substring(0, fieldGetterTypeString.length() - 1);


			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {

				@Override
				public void visit(int version, int access, String name,
						String signature, String superName, String[] interfaces) {
					cv.visit(version, ACC_PUBLIC, AsmUtils.toAsmCls(enhancedClassName), fieldGetterTypeString1 + "<" + entityTypeString + ">;", name, interfaces);
				}

				@Override
				public MethodVisitor visitMethod(int access, String name,
						String desc, String signature, String[] exceptions) {

					if(name.equals(GETTER_METHOD_NAME)) {// get

						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, "(" + entityTypeString + ")Ljava/lang/Object;", null, exceptions);
						//获取this.target
						mv.visitVarInsn(ALOAD, 1);
//						mv.visitFieldInsn(GETFIELD, AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT, entityTypeString);

						mv.visitMethodInsn(INVOKEVIRTUAL,
								AsmUtils.toAsmCls(clazz.getName()), getMethod.getName(),
								mt.toString());

						// 处理返回值类型 到 Object类型
						AsmUtils.withBoxingType(mv, rt);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(1, 1);
						mv.visitEnd();
						return mv;

					} else if(name.equals(GET_NAME_METHOD_NAME)) {//getName
						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						mv.visitLdcInsn(getterName);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(1, 1);
						mv.visitEnd();
					}


					return null;
				}

				@Override
				public void visitEnd() {

					// 基本类型方法桥接泛型方法
					MethodVisitor mv = writer.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, GETTER_METHOD_NAME, "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
					mv.visitCode();
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitTypeInsn(CHECKCAST, AsmUtils.toAsmCls(clazz.getName()));
					mv.visitMethodInsn(INVOKEVIRTUAL, AsmUtils.toAsmCls(enhancedClassName), GETTER_METHOD_NAME, "(" + entityTypeString + ")Ljava/lang/Object;");
					mv.visitInsn(ARETURN);
					mv.visitMaxs(2, 2);
					mv.visitEnd();

					// 调用originalClassName的<init>方法，否则class不能实例化
					MethodVisitor mvInit = writer.visitMethod(ACC_PUBLIC, INIT, "()V",
							null, null);
					mvInit.visitVarInsn(ALOAD, 0);
					mvInit.visitMethodInsn(INVOKESPECIAL, fieldGetterClassName, INIT, "()V");
					mvInit.visitInsn(RETURN);
					mvInit.visitMaxs(1, 1);
					mvInit.visitEnd();

				}

			};

			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();

			AsmUtils.writeClazz(enhancedClassName, byteCodes);
			//load class
			enhancedClass = (Class<T>) classLoader.defineClass(
					enhancedClassName, byteCodes);
		}

		try {
			ValueGetter<T> valueGetter = (ValueGetter<T>) enhancedClass.newInstance();
			return valueGetter;
		} catch (Exception e) {
			logger.error("无法创建代理类对象:" + enhancedClassName);
			throw e;
		}

	}


	/**
	 * 创建属性设值器
	 * @param setterName 名称
	 * @param clazz 类
	 * @param field 属性
	 * @return ValueSetter<T>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> ValueSetter<T> createFieldSetter(final String setterName, final Class<T> clazz, final Field field) throws Exception {
		Class<T> enhancedClass;
		//代理类名
		final String enhancedClassName = AbstractFieldSetter.class.getName()
				+ SUFIX1 + SPLITER + clazz.getSimpleName() + SPLITER
				+ id.incrementAndGet();
		try {
			enhancedClass = (Class<T>) classLoader.loadClass(enhancedClassName);
		} catch (ClassNotFoundException classNotFoundException) {
			ClassReader reader = null;
			try {
				reader = new ClassReader(AbstractFieldSetter.class.getName());
			} catch (IOException ioexception) {
				throw new RuntimeException(ioexception);
			}


			PropertyDescriptor propertyDescriptor = null;
			try {
				propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
			} catch (IntrospectionException e) {
				logger.error("无法获取set方法:" + clazz.getName() + "(" + field.getName() + ")");
				e.printStackTrace();
				throw new EnhanceAccessError("无法获取set方法:" + clazz.getName() + "(" + field.getName() + ")", e);
			}
			//获取set方法
			final Method setMethod = propertyDescriptor.getWriteMethod();
			if (setMethod == null)  {
				logger.error("无法获取set方法:" + clazz.getName() + "(" + field.getName() + ")");
				throw new EnhanceAccessError("无法获取set方法:" + clazz.getName() + "(" + field.getName() + ")", null);
			}
			final Type[] mat = Type.getArgumentTypes(setMethod);
			final Class<?>[] mpt = setMethod.getParameterTypes();
			final Type rt = Type.getReturnType(setMethod);

			final Type mrt = Type.getType(setMethod);

			final String fieldSetterClassName = AsmUtils.toAsmCls(AbstractFieldSetter.class.getName());
			final String entityTypeString = Type.getDescriptor(clazz);
			final String fieldSetterTypeString = Type.getDescriptor(AbstractFieldSetter.class);
			final String fieldSetterTypeString1 = fieldSetterTypeString.substring(0, fieldSetterTypeString.length() - 1);

			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {

				@Override
				public void visit(int version, int access, String name,
								  String signature, String superName, String[] interfaces) {
					cv.visit(version, ACC_PUBLIC, AsmUtils.toAsmCls(enhancedClassName), fieldSetterTypeString1 + "<" + entityTypeString + ">;", name, interfaces);
				}

				@Override
				public MethodVisitor visitMethod(int access, String name,
												 String desc, String signature, String[] exceptions) {

					if(name.equals(SETTER_METHOD_NAME)) {// set

						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, "(" + entityTypeString + "Ljava/lang/Object;)V", null, exceptions);
						//获取this.target
						mv.visitVarInsn(ALOAD, 1);
//						mv.visitFieldInsn(GETFIELD, AsmUtils.toAsmCls(enhancedClassName), REAL_OBJECT, entityTypeString);

						mv.visitVarInsn(ALOAD, 2);

						if(mpt[0].isPrimitive()) {
							// unBoxing
							AsmUtils.withUnBoxingType(mv, mat[0]);
						} else {
							mv.visitTypeInsn(CHECKCAST, AsmUtils.toAsmCls(mpt[0].getName()));
						}

						mv.visitMethodInsn(INVOKEVIRTUAL,
								AsmUtils.toAsmCls(clazz.getName()), setMethod.getName(),
								mrt.toString(), false);

						// 处理返回值类型 到 Object类型
						AsmUtils.withBoxingType(mv, rt);
						if (rt.getSort() == Type.VOID) {
							mv.visitInsn(RETURN);
						} else {
							mv.visitInsn(ARETURN);
						}
						mv.visitMaxs(2, 3);
						mv.visitEnd();
						return mv;

					} else if(name.equals(GET_NAME_METHOD_NAME)) {//getName
						MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
						mv.visitLdcInsn(setterName);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(1, 1);
						mv.visitEnd();
					}


					return null;
				}

				@Override
				public void visitEnd() {

					// 基本类型方法桥接泛型方法
					MethodVisitor mv = writer.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, SETTER_METHOD_NAME, "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
					mv.visitCode();
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitTypeInsn(CHECKCAST, AsmUtils.toAsmCls(clazz.getName()));
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEVIRTUAL, AsmUtils.toAsmCls(enhancedClassName), SETTER_METHOD_NAME, "(" + entityTypeString + "Ljava/lang/Object;)V");
					mv.visitInsn(RETURN);
					mv.visitMaxs(3, 3);
					mv.visitEnd();

					// 调用originalClassName的<init>方法，否则class不能实例化
					MethodVisitor mvInit = writer.visitMethod(ACC_PUBLIC, INIT, "()V",
							null, null);
					mvInit.visitVarInsn(ALOAD, 0);
					mvInit.visitMethodInsn(INVOKESPECIAL, fieldSetterClassName, INIT, "()V");
					mvInit.visitInsn(RETURN);
					mvInit.visitMaxs(1, 1);
					mvInit.visitEnd();

				}

			};

			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();

			AsmUtils.writeClazz(enhancedClassName, byteCodes);
			//load class
			enhancedClass = (Class<T>) classLoader.defineClass(
					enhancedClassName, byteCodes);
		}

		try {
			ValueSetter<T> valueSetter = (ValueSetter<T>) enhancedClass.newInstance();
			return valueSetter;
		} catch (Exception e) {
			logger.error("无法创建代理类对象:" + enhancedClassName);
			throw e;
		}

	}


	/**
	 * 获取修改属性的方法
	 * @param clazz 类
	 * @return 方法 - 修改的属性列表
	 */
	public static Map<Method, List<String>> getPutFieldsMethodMap(final Class<?> clazz) {
		// 查找缓存
		if (putFieldsMthodMapCache.containsKey(clazz)) {
			return putFieldsMthodMapCache.get(clazz);
		}
		
		ClassReader reader = null;
		
		try {
			reader = new ClassReader(clazz.getName());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		
		Map<Method, List<String>> putFieldsMethodMap = new HashMap<Method, List<String>>();
		
        ClassNode cn = new ClassNode(); 
        reader.accept(cn, 0); 
        List<MethodNode> methodList = cn.methods; 
        for (MethodNode md : methodList) {
        	Method method = null;
        	if (md.instructions != null && !"<init>".equals(md.name)) {
            	for (ListIterator<AbstractInsnNode> it = md.instructions.iterator();it.hasNext();) {
            		AbstractInsnNode node = it.next();
            		if (node instanceof FieldInsnNode) {
            			FieldInsnNode fieldNode = ((FieldInsnNode) node);
            			if (fieldNode.getOpcode() == Opcodes.PUTFIELD) {
            				if (method == null) {
            					method = toClassMethod(clazz, md);
            				}
            				if (method == null) {
            					continue;
            				}
            				List<String> fields = putFieldsMethodMap.get(method);
            				if (fields == null) {
            					fields = new ArrayList<String>();
            					putFieldsMethodMap.put(method, fields);
            				}
            				fields.add(fieldNode.name);
            			}
            		}
            	}
            }
        }
        
        putFieldsMthodMapCache.put(clazz, putFieldsMethodMap);
        
        return putFieldsMethodMap;
	}


	/**
	 * 获取修改属性的所有调用方法
	 * @param clazz 类
	 * @return 方法 - 修改的属性列表
	 */
	public static Map<Method, List<String>> getPutFieldsCallHierarchyMethodMap(final Class<?> clazz) {
		// 查找缓存
		if (putFieldsMthodMapCache.containsKey(clazz)) {
			return putFieldsMthodMapCache.get(clazz);
		}

		ClassReader reader = null;

		try {
			reader = new ClassReader(clazz.getName());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}

		String internalClassName = Type.getInternalName(clazz);

		Map<Method, List<String>> putFieldsMethodMap = new HashMap<Method, List<String>>();
		Map<Method, Method> callHierarchyMap = new HashMap<Method, Method>();// key <--call-- value

		ClassNode cn = new ClassNode();
		reader.accept(cn, 0);
		List<MethodNode> methodList = cn.methods;
		for (MethodNode md : methodList) {
			Method method = null;
			if (md.instructions != null && !"<init>".equals(md.name)) {
				for (ListIterator<AbstractInsnNode> it = md.instructions.iterator();it.hasNext();) {
					AbstractInsnNode node = it.next();
					if (node instanceof FieldInsnNode) {
						FieldInsnNode fieldNode = ((FieldInsnNode) node);
						if (fieldNode.getOpcode() == Opcodes.PUTFIELD) {
							if (method == null) {
								method = toClassMethod(clazz, md);
							}
							if (method == null) {
								continue;
							}
							List<String> fields = putFieldsMethodMap.get(method);
							if (fields == null) {
								fields = new ArrayList<String>();
								putFieldsMethodMap.put(method, fields);
							}
							fields.add(fieldNode.name);
						}
					} else if (node instanceof MethodInsnNode) {
						MethodInsnNode methodNode = (MethodInsnNode) node;
						if (methodNode.owner.equals(internalClassName)) {
							if (method == null) {
								method = toClassMethod(clazz, md);
							}
							callHierarchyMap.put(toClassMethod(clazz, methodNode.name, methodNode.desc), method);
						}
					}
				}
			}
		}

		// 处理callHierarchy
		Map<Method, List<String>> addPutFieldsMethods = new HashMap<Method, List<String>>();
		for (Map.Entry<Method, List<String>> putFieldsMethodEntry : putFieldsMethodMap.entrySet()) {
			Method calledMethod = putFieldsMethodEntry.getKey();
			Method callMethod = callHierarchyMap.get(calledMethod);

			while (callMethod != null) {
				if (Modifier.isPublic(callMethod.getModifiers())) {
					addPutFieldsMethods.put(callMethod, putFieldsMethodEntry.getValue());
				}
				calledMethod = callMethod;
				callMethod = callHierarchyMap.get(calledMethod);
			}

		}

		putFieldsMethodMap.putAll(addPutFieldsMethods);

		putFieldsMthodMapCache.put(clazz, putFieldsMethodMap);

		return putFieldsMethodMap;
	}




	private static Method toClassMethod(Class<?> clazz, MethodNode md) {
		return toClassMethod(clazz, md.name, md.desc);
	}

	
	public static Method toClassMethod(Class<?> clazz, String name, String desc) {
		try {
			Class<?>[] parameterTypes = ReflectUtils.parseTypes(desc);
			return clazz.getDeclaredMethod(name, parameterTypes);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	

}


