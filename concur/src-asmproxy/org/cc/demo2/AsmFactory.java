package org.cc.demo2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * 
 * <p>
 * asm 代理工厂
 * </p>
 * 
 * @author dixingxing
 * @date Apr 29, 2012
 */
public class AsmFactory {
	public static final String SUFIX = "$EnhancedByCc";
	public static BytecodeLoader classLoader = new BytecodeLoader();

	/**
	 * 
	 * <p>
	 * 根据字节码加载class
	 * </p>
	 * 
	 * @author dixingxing
	 * @date Apr 29, 2012
	 */
	public static class BytecodeLoader extends ClassLoader {
		public Class<?> defineClass(String className, byte[] byteCodes) {
			return super.defineClass(className, byteCodes, 0, byteCodes.length);
		}
	}

	
	/**
	 * 
	 * <p>
	 * 返回代理类
	 * </p>
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected static <T> Class<T> getEnhancedClass(Class<T> clazz){
		String enhancedClassName = clazz.getName() + SUFIX;
		try {
			return (Class<T>) classLoader.loadClass(enhancedClassName);
		} catch (ClassNotFoundException classNotFoundException) {
			ClassReader reader = null;
			try {
				reader = new ClassReader(clazz.getName());
			} catch (IOException ioexception) {
				throw new RuntimeException(ioexception);
			}
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor visitor = new ClassAdapter(enhancedClassName, clazz,
					writer);
			reader.accept(visitor, 0);
			byte[] byteCodes = writer.toByteArray();
			writeClazz(enhancedClassName, byteCodes);
			Class<T> result = (Class<T>) classLoader.defineClass(
					enhancedClassName, byteCodes);
			return result;
		}
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
			File file = new File("C:" + name + ".class");
			FileOutputStream fout = new FileOutputStream(file);

			fout.write(data);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, NotFoundException, CannotCompileException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		
//		ClassPool classPool = ClassPool.getDefault();
//		
//        CtClass ctClass = classPool.get("org.cc.demo2.Entity");
//        CtMethod ctMethod = ctClass.getDeclaredMethod("setNum");
//        ctMethod.insertAfter("System.out.println(\"this is a new method\");");
//        ctClass.toClass();
		
		Class<Entity> rsCls = getEnhancedClass(Entity.class);
		
		Class<?>[] paramTypes = { Entity.class }; 
		Entity orign = new Entity();
		Object[] params = { orign };  
		Constructor<Entity> con = rsCls.getConstructor(paramTypes);  
		Entity entity = con.newInstance(params);
		
		entity.setNum(2);
		
		System.out.println(entity.getNum());
//		System.out.println(entity.getId());
		
		System.out.println(orign.getNum());
	}

}
