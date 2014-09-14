package dbcache.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 注解工具类
 * @author Jake
 * @date 2014年9月14日下午5:35:32
 */
public class AnnotationUtils implements InvocationHandler {

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> A getDafault(Class<A> annotation) {
		return (A) Proxy.newProxyInstance(annotation.getClassLoader(),
				new Class[] { annotation }, new AnnotationUtils());
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		return method.getDefaultValue();
	}
}