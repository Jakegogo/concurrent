package basesource.test.hotswapagent.getbean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * MethodInterceptor for java.lang.reflect bean Proxies. If the bean inside the proxy is cleared, it will be retrieved
 * from the factory on demand.
 * 
 * @author Erki Ehtla
 * 
 */
public class HotswapSpringInvocationHandler extends DetachableBeanHolder implements InvocationHandler {
	/**
	 * 
	 * @param beanFactry
	 *            Spring beanFactory
	 * @param bean
	 *            Spring bean
	 * @param paramClasses
	 *            Parameter Classes of the Spring beanFactory method which returned the bean. The method is named
	 *            ProxyReplacer.FACTORY_METHOD_NAME
	 * @param paramValues
	 *            Parameter values of the Spring beanFactory method which returned the bean. The method is named
	 *            ProxyReplacer.FACTORY_METHOD_NAME
	 */
	public HotswapSpringInvocationHandler(Object bean, Object beanFactry, Class<?>[] paramClasses, Object[] paramValues) {
		super(bean, beanFactry, paramClasses, paramValues);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return method.invoke(getBean(), args);
	}
}
