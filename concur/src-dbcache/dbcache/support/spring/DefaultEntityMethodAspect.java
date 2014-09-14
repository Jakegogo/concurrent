package dbcache.support.spring;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import dbcache.proxy.AbstractMethodAspect;
import dbcache.proxy.asm.ClassAdapter;
import dbcache.proxy.util.ClassUtil;
import dbcache.service.IndexService;

/**
 * 默认方法切面处理
 * @author Jake
 * @date 2014年9月7日下午6:51:36
 */
@Component("defaultMethodAspect")
public class DefaultEntityMethodAspect extends AbstractMethodAspect {

	/**
	 * 类索引信息
	 * @author Jake
	 * @date 2014年9月7日下午7:50:42
	 */
	class ClassIndexesMetaData {

		/**
		 * 代理类类名
		 */
		String enhancedClassName;

		/**
		 * 索引属性表
		 * 索引名 - 属性
		 */
		Map<String, Field> indexFields = new HashMap<String, Field>();

		/**
		 * 更改索引值的方法列表
		 * 方法 - 索引名
		 */
		Map<Method, Set<MethodMetaData>> changeIndexValueMethods = new HashMap<Method, Set<MethodMetaData>>();

	}


	/**
	 * 方法信息
	 * @author Jake
	 * @date 2014年9月8日下午7:13:28
	 */
	static class MethodMetaData implements Comparable<MethodMetaData> {

		/**
		 * 方法
		 */
		Method method;

		/**
		 * 索引名
		 */
		String indexName;

		/**
		 * asm本地变量计数
		 */
		int local;


		@Override
		public int compareTo(MethodMetaData o) {
			return this.method.hashCode() - o.method.hashCode();
		}


		public static MethodMetaData valueOf(Method method, String indexName) {
			MethodMetaData methodMetaData = new MethodMetaData();
			methodMetaData.method = method;
			methodMetaData.indexName = indexName;
			return methodMetaData;
		}


	}


	/**
	 * 实体索引服务
	 */
	@Autowired
	private IndexService<?> entityIndexService;

	/**
	 * 索引信息缓存
	 * 实体类 - 索引信息
	 */
	private ConcurrentHashMap<Class<?>, ClassIndexesMetaData> CLASS_INDEX_INFO = new ConcurrentHashMap<Class<?>, ClassIndexesMetaData>();


	@Override
	public void initClass(final Class<?> clazz, final String enhancedClassName) {

		final ClassIndexesMetaData indexesMetaData = new ClassIndexesMetaData();
		final Map<Method, Set<MethodMetaData>> methodsMap = indexesMetaData.changeIndexValueMethods;
		final Map<String, Field> fieldsMap = indexesMetaData.indexFields;

		//扫描属性注解
		ReflectionUtils.doWithFields(clazz, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				if (field.isAnnotationPresent(org.hibernate.annotations.Index.class) ||
						field.isAnnotationPresent(dbcache.annotation.Index.class)) {
					String indexName = null;
					org.hibernate.annotations.Index indexAno = field.getAnnotation(org.hibernate.annotations.Index.class);
					if(indexAno != null) {
						indexName = indexAno.name();
					} else {
						dbcache.annotation.Index indexAno1 = field.getAnnotation(dbcache.annotation.Index.class);
						indexName = indexAno1.name();
					}

					try {
						PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
						Method setMethod = propertyDescriptor.getWriteMethod();
						Set<MethodMetaData> methodMetaDataSet = getIndexNameSet(methodsMap, setMethod);
						methodMetaDataSet.add(MethodMetaData.valueOf(setMethod, indexName));

						fieldsMap.put(indexName, field);

					} catch (IntrospectionException e) {
						e.printStackTrace();
					}
				}
			}
		});
		//扫描方法注解
		ReflectionUtils.doWithMethods(clazz, new MethodCallback() {

			@Override
			public void doWith(Method method) throws IllegalArgumentException,
					IllegalAccessException {
				if(method.isAnnotationPresent(dbcache.annotation.UpdateIndex.class)) {
					dbcache.annotation.UpdateIndex updateIndexAno = method.getAnnotation(dbcache.annotation.UpdateIndex.class);
					for(String indexName : updateIndexAno.value()) {
						Set<MethodMetaData> methodMetaDataSet = getIndexNameSet(methodsMap, method);
						methodMetaDataSet.add(MethodMetaData.valueOf(method, indexName));
					}
				}
			}
		});

		indexesMetaData.enhancedClassName = enhancedClassName;

		CLASS_INDEX_INFO.putIfAbsent(clazz, indexesMetaData);

	}


	private Set<MethodMetaData> getIndexNameSet(Map<Method, Set<MethodMetaData>> methodsMap, Method method) {
		Set<MethodMetaData> methodMetaData = methodsMap.get(method);
		if(methodMetaData == null) {
			methodMetaData = new TreeSet<MethodMetaData>();
			methodsMap.put(method, methodMetaData);
		}
		return methodMetaData;
	}


	@Override
	public int doBefore(MethodVisitor mWriter, Method method, int locals) {
		Class<?> entityClass = method.getDeclaringClass();
		ClassIndexesMetaData classIndexesMetaData = CLASS_INDEX_INFO.get(entityClass);
		if(classIndexesMetaData == null) {
			return locals;
		}
		final Map<Method, Set<MethodMetaData>> methodsMap = classIndexesMetaData.changeIndexValueMethods;
		if(!methodsMap.containsKey(method)) {
			return locals;
		}
		Set<MethodMetaData> methodMetaDatas = methodsMap.get(method);

		final Map<String, Field> fieldsMap = classIndexesMetaData.indexFields;

		for(MethodMetaData methodMetaData : methodMetaDatas) {
			final Field field = fieldsMap.get(methodMetaData.indexName);
			//获取this.obj.fieldName
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
			mWriter.visitFieldInsn(Opcodes.GETFIELD,ClassUtil.toAsmCls(classIndexesMetaData.enhancedClassName), ClassAdapter.REAL_OBJECT, Type.getDescriptor(entityClass));

			PropertyDescriptor propertyDescriptor = null;
			try {
				propertyDescriptor = new PropertyDescriptor(field.getName(), entityClass);
			} catch (IntrospectionException e) {
				e.printStackTrace();
				return locals;
			}
			Method getMethod = propertyDescriptor.getReadMethod();

			Type mt = Type.getType(getMethod);
			mWriter.visitMethodInsn(INVOKEVIRTUAL,
					ClassUtil.toAsmCls(field.getDeclaringClass().getName()), getMethod.getName(),
					mt.toString());

			// 处理返回值类型 到 Object类型
			Type rt = Type.getReturnType(getMethod);
			ClassUtil.withBoxingType(mWriter, rt);

			//存储到变量
			mWriter.visitVarInsn(Opcodes.ASTORE, locals);

			methodMetaData.local = locals;

			locals ++;
		}


		return locals;
	}

	@Override
	public int doAfter(MethodVisitor mWriter, Method method, int locals) {
		Class<?> entityClass = method.getDeclaringClass();
		ClassIndexesMetaData classIndexesMetaData = CLASS_INDEX_INFO.get(entityClass);
		if(classIndexesMetaData == null) {
			return locals;
		}
		final Map<Method, Set<MethodMetaData>> methodsMap = classIndexesMetaData.changeIndexValueMethods;
		if(!methodsMap.containsKey(method)) {
			return locals;
		}
		Set<MethodMetaData> methodMetaDatas = methodsMap.get(method);

		final Map<String, Field> fieldsMap = classIndexesMetaData.indexFields;

		for(MethodMetaData methodMetaData : methodMetaDatas) {
			locals ++;

			final Field field = fieldsMap.get(methodMetaData.indexName);
			//获取this.obj.getFieldName
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
			mWriter.visitFieldInsn(Opcodes.GETFIELD,ClassUtil.toAsmCls(classIndexesMetaData.enhancedClassName), ClassAdapter.REAL_OBJECT, Type.getDescriptor(entityClass));

			PropertyDescriptor propertyDescriptor = null;
			try {
				propertyDescriptor = new PropertyDescriptor(field.getName(), entityClass);
			} catch (IntrospectionException e) {
				e.printStackTrace();
				return locals;
			}
			Method getMethod = propertyDescriptor.getReadMethod();

			Type mt = Type.getType(getMethod);
			mWriter.visitMethodInsn(INVOKEVIRTUAL,
					ClassUtil.toAsmCls(field.getDeclaringClass().getName()), getMethod.getName(),
					mt.toString());

			// 处理返回值类型  到 Object类型
			Type rt = Type.getReturnType(getMethod);
			ClassUtil.withBoxingType(mWriter, rt);

			//存储到变量
			mWriter.visitVarInsn(Opcodes.ASTORE, locals);

			//调用 changeIndex(Object entity, String indexName, Object oldValue, Object newValue)
			//获取this.handler
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
			mWriter.visitFieldInsn(Opcodes.GETFIELD, ClassUtil.toAsmCls(classIndexesMetaData.enhancedClassName), ClassAdapter.HANDLER_OBJECT, Type.getDescriptor(this.getAspectHandleClass()));

			//获取this.obj
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
			mWriter.visitFieldInsn(Opcodes.GETFIELD,ClassUtil.toAsmCls(classIndexesMetaData.enhancedClassName), ClassAdapter.REAL_OBJECT, Type.getDescriptor(entityClass));

			//load indexName
			mWriter.visitLdcInsn(methodMetaData.indexName);

			//获取属性修改前的值
			mWriter.visitVarInsn(Opcodes.ALOAD, methodMetaData.local);

			//获取属性修改后的值
			mWriter.visitVarInsn(Opcodes.ALOAD, locals);

			//调用静态方法
			mWriter.visitMethodInsn(INVOKEINTERFACE, ClassUtil.toAsmCls(getAspectHandleClass().getName()), "update", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");

		}

		return locals;
	}



	@Override
	public Class<?> getAspectHandleClass() {
		return IndexService.class;
	}


}
