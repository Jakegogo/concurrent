package dbcache.support.asm;

import dbcache.EnhancedEntity;
import dbcache.index.DbIndexService;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import utils.enhance.asm.AsmAccessHelper;
import utils.enhance.asm.util.AsmUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认方法切面代理处理
 * @author Jake
 * @date 2014年9月7日下午6:51:36
 */
@Component("defaultMethodAspect")
public class IndexMethodProxyAspect extends AbstractAsmMethodProxyAspect {


	/**
	 * 索引信息缓存
	 * 实体类 - 索引信息
	 */
	private ConcurrentHashMap<Class<?>, ClassIndexesMetaData> CLASS_INDEX_INFO = new ConcurrentHashMap<Class<?>, ClassIndexesMetaData>();



	@Override
	public void doInitClass(ConstructorBuilder constructorBuilder) {

		// 增加切面处理对象
		constructorBuilder.appendField(getAspectHandleClass(), EntityClassProxyAdapter.HANDLER_OBJECT);


		// 添加切面处理对象构造方法,用真实类对象作为参数
		constructorBuilder.appendParameter(new ConstructorBuilder.ParameterInit () {

			@Override
			Class<?> parameterType() {
				return getAspectHandleClass();
			}

			@Override
			/**
			 * @see dbcache.support.asm.ConstructorBuilder#getProxyEntity(java.lang.Class<T>, T, dbcache.index.DbIndexService , java.util.concurrent.atomic.AtomicIntegerArray)
			 */
			public int parameterIndexOfgetProxyEntity() {
				return 1;
			}

			@Override
			public void onConstruct(
					ClassWriter classWriter,
					MethodVisitor mvInit,
					Class<?> originalClass,
					String enhancedClassName,
					int localIndex) {
				mvInit.visitVarInsn(Opcodes.ALOAD, 0);
				mvInit.visitVarInsn(Opcodes.ALOAD, localIndex);

				mvInit.visitFieldInsn(Opcodes.PUTFIELD,
						AsmUtils.toAsmCls(enhancedClassName),
						EntityClassProxyAdapter.HANDLER_OBJECT,
						Type.getDescriptor(getAspectHandleClass()));
			}
		});
				
	}

	@Override
	public void initClassMetaInfo(final Class<?> clazz, final String enhancedClassName) {

		final ClassIndexesMetaData indexesMetaData = new ClassIndexesMetaData();
		final Map<Method, Set<MethodMetaData>> methodsMap = indexesMetaData.changeIndexValueMethods;// 方法 - 修改的索引集合
		final Map<String, Field> fieldsMap = indexesMetaData.indexFields;// 索引名 - 属性
		final Map<String, String> indexesMap = new HashMap<String, String>();// 属性名 - 索引名

		//扫描属性注解
		ReflectionUtils.doWithFields(clazz, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				if (field.isAnnotationPresent(org.hibernate.annotations.Index.class) ||
						field.isAnnotationPresent(dbcache.anno.Index.class)) {
					org.hibernate.annotations.Index indexAno = field.getAnnotation(org.hibernate.annotations.Index.class);

					String indexName;
					if(indexAno != null) {
						indexName = indexAno.name();
					} else {
						dbcache.anno.Index indexAno1 = field.getAnnotation(dbcache.anno.Index.class);
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

					indexesMap.put(field.getName(), indexName);
				}
			}
		});
		
		// 扫描修改属性的方法
		Map<Method, List<String>> putFieldMethods = AsmAccessHelper.getPutFieldsCallHierarchyMethodMap(clazz);
		
		for (Map.Entry<Method, List<String>> methodEntry : putFieldMethods.entrySet()) {

			List<String> modifields = methodEntry.getValue();
			for (String field : modifields) {
				if (indexesMap.containsKey(field)) {
					Method method = methodEntry.getKey();
					Set<MethodMetaData> methodMetaDataSet = getIndexNameSet(methodsMap, method);
					
					String indexName = indexesMap.get(field);
					methodMetaDataSet.add(MethodMetaData.valueOf(method, indexName));
				}
			}
		}

		indexesMetaData.enhancedClassName = enhancedClassName;
		//存储到缓存
		CLASS_INDEX_INFO.putIfAbsent(clazz, indexesMetaData);

	}

	//获取索引对应的方法集合
	private Set<MethodMetaData> getIndexNameSet(Map<Method, Set<MethodMetaData>> methodsMap, Method method) {
		Set<MethodMetaData> methodMetaData = methodsMap.get(method);
		if(methodMetaData == null) {
			methodMetaData = new TreeSet<MethodMetaData>();
			methodsMap.put(method, methodMetaData);
		}
		return methodMetaData;
	}


	@Override
	public int doBefore(
			Class<?> entityClass,
			MethodVisitor mWriter,
			Method method,
			int locals,
			String name,
			int acc,
			String desc) {
		
		//获取类信息
		ClassIndexesMetaData classIndexesMetaData = CLASS_INDEX_INFO.get(entityClass);
		if(classIndexesMetaData == null) {
			return locals;
		}
		
		//获取需要拦截的方法列表
		final Map<Method, Set<MethodMetaData>> methodsMap = classIndexesMetaData.changeIndexValueMethods;
		if(!methodsMap.containsKey(method)) {
			return locals;
		}
		

		//获取索引属性
		final Map<String, Field> fieldsMap = classIndexesMetaData.indexFields;


		// 遍历需要处理的方法
		Set<MethodMetaData> methodMetaDatas = methodsMap.get(method);
		for (MethodMetaData methodMetaData : methodMetaDatas) {
			//获取属性
			final Field field = fieldsMap.get(methodMetaData.indexName);
			//获取this.obj.fieldName
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
			mWriter.visitFieldInsn(
					Opcodes.GETFIELD,
					AsmUtils.toAsmCls(classIndexesMetaData.enhancedClassName),
					EntityClassProxyAdapter.REAL_OBJECT,
					Type.getDescriptor(entityClass));

			PropertyDescriptor propertyDescriptor = null;
			try {
				propertyDescriptor = new PropertyDescriptor(field.getName(), entityClass);
			} catch (IntrospectionException e) {
				e.printStackTrace();
				return locals;
			}
			Method getMethod = propertyDescriptor.getReadMethod();

			Type mt = Type.getType(getMethod);
			mWriter.visitMethodInsn(
					INVOKEVIRTUAL,
					AsmUtils.toAsmCls(field.getDeclaringClass().getName()),
					getMethod.getName(),
					mt.toString());

			// 处理返回值类型 到 Object类型
			Type rt = Type.getReturnType(getMethod);
			AsmUtils.withBoxingType(mWriter, rt);

			//存储到变量
			mWriter.visitVarInsn(Opcodes.ASTORE, locals);

			methodMetaData.local = locals;

			locals ++;
		}


		return locals;
	}

	@Override
	public int doAfter(
			Class<?> entityClass,
			MethodVisitor mWriter,
			Method method,
			int locals,
			String name,
			int acc,
			String desc) {
		
		//获取类信息
		ClassIndexesMetaData classIndexesMetaData = CLASS_INDEX_INFO.get(entityClass);
		if(classIndexesMetaData == null) {
			return locals;
		}
		//获取需要拦截的方法列表
		final Map<Method, Set<MethodMetaData>> methodsMap = classIndexesMetaData.changeIndexValueMethods;
		if(!methodsMap.containsKey(method)) {
			return locals;
		}
		
		//获取索引属性
		final Map<String, Field> fieldsMap = classIndexesMetaData.indexFields;

		// 遍历需要处理的方法
		Set<MethodMetaData> methodMetaDatas = methodsMap.get(method);
		for(MethodMetaData methodMetaData : methodMetaDatas) {

			locals ++;
			//获取属性
			final Field field = fieldsMap.get(methodMetaData.indexName);
			//获取this.obj.getFieldName
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
			mWriter.visitFieldInsn(
					Opcodes.GETFIELD,
					AsmUtils.toAsmCls(classIndexesMetaData.enhancedClassName),
					EntityClassProxyAdapter.REAL_OBJECT,
					Type.getDescriptor(entityClass));


			PropertyDescriptor propertyDescriptor;
			try {
				propertyDescriptor = new PropertyDescriptor(field.getName(), entityClass);
			} catch (IntrospectionException e) {
				e.printStackTrace();
				return locals;
			}


			Method getMethod = propertyDescriptor.getReadMethod();


			Type mt = Type.getType(getMethod);
			mWriter.visitMethodInsn(INVOKEVIRTUAL,
					AsmUtils.toAsmCls(field.getDeclaringClass().getName()), getMethod.getName(),
					mt.toString());

			// 处理返回值类型  到 Object类型
			Type rt = Type.getReturnType(getMethod);
			AsmUtils.withBoxingType(mWriter, rt);

			//存储到变量
			mWriter.visitVarInsn(Opcodes.ASTORE, locals);

			//调用 changeIndex(Object entity, String indexName, Object oldValue, Object newValue)
			//获取this.handler
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
			mWriter.visitFieldInsn(
					Opcodes.GETFIELD,
					AsmUtils.toAsmCls(classIndexesMetaData.enhancedClassName),
					EntityClassProxyAdapter.HANDLER_OBJECT,
					Type.getDescriptor(this.getAspectHandleClass()));

			//获取this
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
//			mWriter.visitFieldInsn(
//					Opcodes.GETFIELD,
//					AsmUtils.toAsmCls(classIndexesMetaData.enhancedClassName),
//					EntityClassProxyAdapter.REAL_OBJECT,
//					Type.getDescriptor(entityClass));

			//load indexName
			mWriter.visitLdcInsn(methodMetaData.indexName);

			//获取属性修改前的值
			mWriter.visitVarInsn(Opcodes.ALOAD, methodMetaData.local);

			//获取属性修改后的值
			mWriter.visitVarInsn(Opcodes.ALOAD, locals);

			//调用静态方法
			mWriter.visitMethodInsn(
					INVOKEINTERFACE,
					AsmUtils.toAsmCls(getAspectHandleClass().getName()),
					"update",
					"(" + Type.getDescriptor(EnhancedEntity.class) + "Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");

		}

		return locals;
	}

	@Override
	public boolean needOverride(Class<?> entityClass, Method method) {
		//获取类信息
		ClassIndexesMetaData classIndexesMetaData = CLASS_INDEX_INFO.get(entityClass);
		if(classIndexesMetaData == null) {
			return false;
		}
		//获取需要拦截的方法列表
		return classIndexesMetaData.changeIndexValueMethods.containsKey(method);
	}


	@Override
	public Class<?> getAspectHandleClass() {
		return DbIndexService.class;
	}


	/**
	 * 类索引信息
	 * @author Jake
	 * @date 2014年9月7日下午7:50:42
	 */
	static class ClassIndexesMetaData {

		/** 代理类类名 */
		String enhancedClassName;

		/** 索引属性表  索引名 - 属性 */
		Map<String, Field> indexFields = new HashMap<String, Field>();

		/** 更改索引值的方法列表 方法 - 索引名 */
		Map<Method, Set<MethodMetaData>> changeIndexValueMethods = new HashMap<Method, Set<MethodMetaData>>();

	}


	/**
	 * 方法信息
	 * @author Jake
	 * @date 2014年9月8日下午7:13:28
	 */
	static class MethodMetaData implements Comparable<MethodMetaData> {

		/** 方法 */
		Method method;

		/**  索引名 */
		String indexName;

		/**  asm本地变量计数 */
		int local;


		@Override
		public int compareTo(MethodMetaData o) {
			return this.indexName.hashCode() - o.indexName.hashCode();
		}


		public static MethodMetaData valueOf(Method method, String indexName) {
			MethodMetaData methodMetaData = new MethodMetaData();
			methodMetaData.method = method;
			methodMetaData.indexName = indexName;
			return methodMetaData;
		}


	}


}
