package dbcache.support.spring;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import dbcache.model.IEntity;
import dbcache.proxy.asm.AsmFactory;
import dbcache.service.Cache;
import dbcache.service.DbCacheService;
import dbcache.service.impl.DbCacheServiceImpl;

/**
 * DbCache自动注入处理器
 * @author Jake
 * @date 2014年8月24日下午7:58:00
 */
@Component
public class DbCacheInjectProcessor extends InstantiationAwareBeanPostProcessorAdapter {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DbCacheInjectProcessor.class);


	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private DefaultMethodAspect methodAspect;


	@SuppressWarnings("rawtypes")
	private Map<Class<?>, DbCacheService> dbCacheServiceBeanMap = new ConcurrentHashMap<Class<?>, DbCacheService>();


	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName)
			throws BeansException {
		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				if (field.getType().equals(DbCacheService.class)) {
					// 注入实体单位缓存服务
					injectDbCacheService(bean, beanName, field);
				}
			}
		});
		return super.postProcessAfterInitialization(bean, beanName);
	}


	/**
	 * 注入DbCache
	 * @param bean bean
	 * @param beanName beanName
	 * @param field field
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void injectDbCacheService(Object bean, String beanName,
			Field field) {

		Class<? extends IEntity> clz = null;
		DbCacheService service = null;

		try {
			Type type = field.getGenericType();
			Type[] types = ((ParameterizedType) type).getActualTypeArguments();
			clz = (Class<? extends IEntity>) types[0];
			service = this.getDbCacheServiceBean(clz);
		} catch (Exception e) {
			FormattingTuple message = MessageFormatter.arrayFormat("Bean[{}]的注入属性[{}<{}, ?>]类型声明错误", new Object[]{beanName, field.getName(), clz != null ?clz.getSimpleName() : null});
			logger.error(message.getMessage());
			throw new IllegalStateException(message.getMessage(), e);
		}
		if (service == null) {
			FormattingTuple message = MessageFormatter.format("实体[{}]缓存服务对象不存在", clz.getName());
			logger.debug(message.getMessage());
			throw new IllegalStateException(message.getMessage());
		}

		//注入DbCacheService
		inject(bean, field, service);

	}



	@SuppressWarnings({ "rawtypes" })
	private DbCacheService getDbCacheServiceBean(Class<? extends IEntity> clz) throws NoSuchFieldException, SecurityException {

		DbCacheService service = this.dbCacheServiceBeanMap.get(clz);

		if(service == null) {
			//创建新的bean
			service = applicationContext.getAutowireCapableBeanFactory().createBean(DbCacheServiceImpl.class);

			//设置实体类
			Field clazzField = DbCacheServiceImpl.class.getDeclaredField("clazz");
			inject(service, clazzField, clz);


			//初始化代理类
			Class<?> proxyClazz = AsmFactory.getEnhancedClass(clz, methodAspect);
			Field proxyClazzField = DbCacheServiceImpl.class.getDeclaredField("proxyClazz");
			inject(service, proxyClazzField, proxyClazz);


			//初始化缓存实例
			Field cacheField = DbCacheServiceImpl.class.getDeclaredField("cache");
			Class<?> cacheClass = service.getCache().getClass();
			Cache cache = (Cache) applicationContext.getAutowireCapableBeanFactory().createBean(cacheClass);
			inject(service, cacheField, cache);

			dbCacheServiceBeanMap.put(clz, service);

		}

		return service;
	}


	/**
	 * 注入属性
	 * @param bean bean
	 * @param field 属性
	 * @param val 值
	 */
	private void inject(Object bean, Field field, Object val) {
		ReflectionUtils.makeAccessible(field);
		try {
			field.set(bean, val);
		} catch (Exception e) {
			FormattingTuple message = MessageFormatter.format("属性[{}]注入失败", field);
			logger.debug(message.getMessage());
			throw new IllegalStateException(message.getMessage(), e);
		}
	}


}
