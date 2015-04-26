package dbcache.support.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;

/**
 * 抽象切面注入
 * @author Jake
 * @date 2014年9月7日下午6:40:52
 */
public abstract class AbstractAsmMethodProxyAspect implements Opcodes {


	/**
	 * 初始化类的切面信息
	 * @param enhancedClassName 代理类类名
	 */
	public void initClassMetaInfo(Class<?> clazz, String enhancedClassName) {
		//do nothing
	}
	
	
	/**
	 * 初始化类
	 * @param constructorBuilder 构造方法构建器
	 */
	public void doInitClass(ConstructorBuilder constructorBuilder){}
	
	/**
	 * 方法执行前执行
	 * @param entityClass TODO
	 * @param mWriter MethodVisitor
	 * @param method 方法
	 * @param locals 本地变量数量
	 * @param name 方法名
	 * @param acc 访问权限
	 * @param desc 描述
	 * @return
	 */
	public int doBefore(Class<?> entityClass, MethodVisitor mWriter, Method method, int locals, String name, int acc, String desc) {
		//do nothing
		return locals;
	}

	/**
	 * 方法执行后执行
	 * @param entityClass TODO
	 * @param mWriter MethodVisitor
	 * @param method 方法
	 * @param locals 本地变量数量
	 * @param name TODO
	 * @param acc TODO
	 * @param desc TODO
	 * @return
	 */
	public int doAfter(Class<?> entityClass, MethodVisitor mWriter, Method method, int locals, String name, int acc, String desc) {
		//do nothing
		return locals;
	}


	/**
	 * 判断是否需要重写
	 * @param entityClass 实体类
	 * @param method 方法
	 * @return
	 */
	public boolean needOverride(Class<?> entityClass, Method method) {
		return false;
	}


	/**
	 * 获取切面处理类
	 * @return
	 */
	public Class<?> getAspectHandleClass() {
		return Object.class;
	}

}

