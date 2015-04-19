package utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ReflectionUtils;

/**
 * 反射工具类
 * @see {@link ReflectionUtils}
 * @author frank
 */
public abstract class ReflectionUtility extends ReflectionUtils {

	/**
	 * 查找唯一被指定注释声明的域
	 * @param <A> 注释类型
	 * @param clz 被查找的类
	 * @param type 指定的注释
	 * @return 不存在会返回 null
	 */
	public static <A extends Annotation> Field findUniqueFieldWithAnnotation(Class<?> clz, final Class<A> type) {
		final List<Field> fields = new ArrayList<Field>();
		doWithFields(clz, new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				fields.add(field);
			}
		}, new FieldFilter() {
			@Override
			public boolean matches(Field field) {
				return field.isAnnotationPresent(type);
			}
		});

		if (fields.size() > 1) {
			throw new IllegalStateException("被注释" + type.getSimpleName() + "声明的域不唯一");
		} else if (fields.size() == 1) {
			return fields.get(0);
		}
		return null;
	}

	/**
	 * 类似{@link org.springframework.util.ReflectionUtils#doWithFields(Class, FieldCallback, FieldFilter)}
	 * 的方法，只是该方法不会递归检查父类上的域
	 * @see org.springframework.util.ReflectionUtils#doWithFields(Class, FieldCallback, FieldFilter)
	 * @param clazz
	 * @param fc
	 * @param ff
	 * @throws IllegalArgumentException
	 */
	public static void doWithDeclaredFields(Class<?> clazz, FieldCallback fc, FieldFilter ff) throws IllegalArgumentException {
		if (clazz == null || clazz == Object.class) {
			return;
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (ff != null && !ff.matches(field)) {
				continue;
			}
			try {
				fc.doWith(field);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("非法访问属性 '" + field.getName() + "': " + ex);
			}
		}
	}

	/**
	 * 获得第一个使用指定注释声明的属性
	 * @param clz 属性所在类
	 * @param annotationClass 注释类型
	 * @return 不存在则返回 null
	 */
	public static Field getFirstDeclaredFieldWith(Class<?> clz, Class<? extends Annotation> annotationClass) {
		for (Field field : clz.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotationClass)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * 获得全部使用指定注释声明的属性
	 * @param clz 属性所在类
	 * @param annotationClass 注释类型
	 * @return 不会返回 null
	 */
	public static Field[] getDeclaredFieldsWith(Class<?> clz, final Class<? extends Annotation>... annotationClass) {
		final List<Field> fields = new ArrayList<Field>();
		ReflectionUtils.doWithFields(clz, new FieldCallback() {

			@Override
			public void doWith(Field field) throws IllegalArgumentException,
					IllegalAccessException {
				for(Class<? extends Annotation> annoClazz : annotationClass) {
					if (field.isAnnotationPresent(annoClazz)) {
						fields.add(field);
					}
				}
			}
		});

		return fields.toArray(new Field[0]);
	}

	/**
	 * 获得第一个使用指定注释声明的方法
	 * @param clz 属性所在类
	 * @param annotationClass 注释类型
	 * @return 不存在则返回 null
	 */
	public static Method getFirstDeclaredMethodWith(Class<?> clz, Class<? extends Annotation> annotationClass) {
		for (Method method : clz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotationClass)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * 获得全部使用指定注释声明的方法
	 * @param clz 属性所在类
	 * @param annotationClass 注释类型
	 * @return 不会返回 null
	 */
	public static Method[] getDeclaredMethodsWith(Class<?> clz, Class<? extends Annotation> annotationClass) {
		List<Method> methods = new ArrayList<Method>();
		for (Method method : clz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotationClass)) {
				methods.add(method);
			}
		}
		return methods.toArray(new Method[0]);
	}

	/**
	 * 获得全部使用指定注释声明的 get 方法
	 * @param clz 属性所在类
	 * @param annotationClass 注释类型
	 * @return 不会返回 null
	 */
	public static Method[] getDeclaredGetMethodsWith(Class<?> clz, Class<? extends Annotation> annotationClass) {
		List<Method> methods = new ArrayList<Method>();
		for (Method method : clz.getDeclaredMethods()) {
			if (method.getAnnotation(annotationClass) == null) {
				continue;
			}
			if (method.getReturnType().equals(void.class)) {
				continue;
			}
			if (method.getParameterTypes().length > 0) {
				continue;
			}
			methods.add(method);
		}
		return methods.toArray(new Method[0]);
	}

}
