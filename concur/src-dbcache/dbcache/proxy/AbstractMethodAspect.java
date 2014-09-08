package dbcache.proxy;

import java.lang.reflect.Method;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 抽象切面注入
 * @author Jake
 * @date 2014年9月7日下午6:40:52
 */
public abstract class AbstractMethodAspect implements Opcodes {


	/**
	 * 初始化类的切面信息
	 * @param enhancedClassName 代理类类名
	 */
	public void initClass(Class<?> clazz, String enhancedClassName) {
		//do nothing
	}

	/**
	 * 方法执行前执行
	 * @param mWriter MethodVisitor
	 * @param method 方法
	 * @param locals 本地变量数量
	 * @return
	 */
	public int doBefore(MethodVisitor mWriter, Method method, int locals) {
		//do nothing
		return locals;
	}

	/**
	 * 方法执行后执行
	 * @param mWriter MethodVisitor
	 * @param method 方法
	 * @param locals 本地变量数量
	 * @return
	 */
	public int doAfter(MethodVisitor mWriter, Method method, int locals) {
		//do nothing
		return 0;
	}

}

