package dbcache.support.asm;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import dbcache.annotation.ChangeFields;
import dbcache.support.asm.IndexMethodProxyAspect.MethodMetaData;
import dbcache.support.asm.util.AsmUtils;
import dbcache.utils.IntegerCounter;

/**
 * 记录修改属性的切面
 * Created by Jake on 2015/2/8.
 */
@Component
public class ModifiedFieldMethodAspect extends AbstractAsmMethodProxyAspect {
	
	/**
	 * 已经修改过的字段数组,0:未修改; 2:修改过
	 */
	private static final String CHANGE_FIELDS_ARRAY = "changeFields";
	
	/**
	 * 索引信息缓存
	 * 实体类 - 索引信息
	 */
	private ConcurrentHashMap<Class<?>, ClassIndexesMetaData> CLASS_INDEX_INFO = new ConcurrentHashMap<Class<?>, ClassIndexesMetaData>();



	@Override
	public void doInitClass(ConstructorBuilder constructorBuilder) {
		
		// 增加原实体类型的属性(真实类)
		constructorBuilder.appendField(AtomicIntegerArray.class, CHANGE_FIELDS_ARRAY);

		// 添加切面处理对象构造方法,用真实类对象作为参数
		constructorBuilder.appendParameter(AtomicIntegerArray.class, new ConstructorBuilder.ParameterInit () {

			@Override
			/**
			 * @see dbcache.support.asm.ConstructorBuilder#getProxyEntity(java.lang.Class<T>, T, dbcache.service.DbIndexService, java.util.concurrent.atomic.AtomicIntegerArray)
			 */
			public int parameterIndexOfgetProxyEntity() {
				return 2;
			}

			@Override
			public void onConstruct(ClassWriter classWriter, MethodVisitor mvInit, Class<?> originalClass, String enhancedClassName, int localIndex) {
				mvInit.visitVarInsn(Opcodes.ALOAD, 0);
				mvInit.visitVarInsn(Opcodes.ALOAD, localIndex);

				mvInit.visitFieldInsn(Opcodes.PUTFIELD,
						AsmUtils.toAsmCls(enhancedClassName), CHANGE_FIELDS_ARRAY,
						Type.getDescriptor(AtomicIntegerArray.class));
			}
		});
				
	}

	@Override
	public void initClassMetaInfo(final Class<?> clazz, final String enhancedClassName) {

		final ClassIndexesMetaData indexesMetaData = new ClassIndexesMetaData();
		final Map<Method, Set<MethodMetaData>> methodsMap = indexesMetaData.changeIndexValueMethods;// 修改属性的方法 - 修改到的属性集合
		final Map<String, FieldMetaData> fieldsMap = indexesMetaData.fields;// 属性名 - 属性信息

		//扫描属性注解
		final IntegerCounter fieldIndexCounter = new IntegerCounter();
		ReflectionUtils.doWithFields(clazz, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				// 忽略静态属性和临时属性
				if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) ||
						field.isAnnotationPresent(javax.persistence.Transient.class)) {
					return;
				}
				
				int fieldIndex = fieldIndexCounter.getAndIncrement();
				try {
					PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
					Method setMethod = propertyDescriptor.getWriteMethod();
					Set<MethodMetaData> methodMetaDataSet = getFieldNameSet(methodsMap, setMethod);
					methodMetaDataSet.add(MethodMetaData.valueOf(setMethod, field.getName()));

					FieldMetaData fieldMetaData = new FieldMetaData();
					fieldMetaData.field = field;
					fieldMetaData.fieldIndex = fieldIndex;
					
					fieldsMap.put(field.getName(), fieldMetaData);

				} catch (IntrospectionException e) {
					e.printStackTrace();
				}
			}
		});
		
		//扫描方法注解
		ReflectionUtils.doWithMethods(clazz, new MethodCallback() {

			@Override
			public void doWith(Method method) throws IllegalArgumentException,
					IllegalAccessException {
				if (method.isAnnotationPresent(ChangeFields.class)) {
					ChangeFields updateFieldsAno = method.getAnnotation(ChangeFields.class);
					Set<MethodMetaData> methodMetaDataSet = getFieldNameSet(methodsMap, method);
					for(String fieldName : updateFieldsAno.value()) {
						methodMetaDataSet.add(MethodMetaData.valueOf(method, fieldName));
					}
				}
			}
		});
		
		// 扫描修改属性的方法
		Map<Method, List<String>> putFieldMethods = AsmAccessHelper.getPutFieldsMethodMap(clazz);
		
		for (Map.Entry<Method, List<String>> methodEntry : putFieldMethods.entrySet()) {
			List<String> modifields = methodEntry.getValue();
			for (String field : modifields) {
				if (fieldsMap.containsKey(field)) {
					Method method = methodEntry.getKey();
					Set<MethodMetaData> methodMetaDataSet = getFieldNameSet(methodsMap, method);
					methodMetaDataSet.add(MethodMetaData.valueOf(method, field));
				}
			}
		}
		

		indexesMetaData.enhancedClassName = enhancedClassName;
		//存储到缓存
		CLASS_INDEX_INFO.putIfAbsent(clazz, indexesMetaData);

	}

	//获取索引对应的方法集合
	private Set<MethodMetaData> getFieldNameSet(Map<Method, Set<MethodMetaData>> methodsMap, Method method) {
		Set<MethodMetaData> methodMetaData = methodsMap.get(method);
		if(methodMetaData == null) {
			methodMetaData = new TreeSet<MethodMetaData>();
			methodsMap.put(method, methodMetaData);
		}
		return methodMetaData;
	}


	@Override
	public int doBefore(Class<?> entityClass, MethodVisitor mWriter, Method method, int locals, String name, int acc, String desc) {
		
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
		
		Set<MethodMetaData> methodMetaDatas = methodsMap.get(method);
		//获取索引属性
		final Map<String, FieldMetaData> fieldsMap = classIndexesMetaData.fields;

		for (MethodMetaData methodMetaData : methodMetaDatas) {
			//获取属性
			final FieldMetaData field = fieldsMap.get(methodMetaData.fieldName);
			//获取this.obj.fieldName
			mWriter.visitVarInsn(Opcodes.ALOAD, 0);
			mWriter.visitFieldInsn(Opcodes.GETFIELD, AsmUtils.toAsmCls(classIndexesMetaData.enhancedClassName), CHANGE_FIELDS_ARRAY,
					Type.getDescriptor(AtomicIntegerArray.class));

			mWriter.visitLdcInsn(field.fieldIndex);
			mWriter.visitLdcInsn(1);
			
			Method setMethod = null;
			try {
				setMethod = AtomicIntegerArray.class.getDeclaredMethod("set", int.class, int.class);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			
			Type mt = Type.getType(setMethod);
			mWriter.visitMethodInsn(INVOKEVIRTUAL,
					AsmUtils.toAsmCls(AtomicIntegerArray.class.getName()), "set",
					mt.toString());
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
		final Map<Method, Set<MethodMetaData>> methodsMap = classIndexesMetaData.changeIndexValueMethods;
		if(!methodsMap.containsKey(method)) {
			return false;
		}
		return true;
	}


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
		Map<String, FieldMetaData> fields = new LinkedHashMap<String, FieldMetaData>();

		/**
		 * 更改索引值的方法列表
		 * 方法 - 索引名
		 */
		Map<Method, Set<MethodMetaData>> changeIndexValueMethods = new LinkedHashMap<Method, Set<MethodMetaData>>();

	}


	/**
	 * 属性信息
	 * @author Jake
	 *
	 */
	static class FieldMetaData {
		
		private Field field;
		
		private int fieldIndex;
		
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
		 * 属性名
		 */
		String fieldName;


		@Override
		public int compareTo(MethodMetaData o) {
			return this.fieldName.hashCode() - o.fieldName.hashCode();
		}


		public static MethodMetaData valueOf(Method method, String fieldName) {
			MethodMetaData methodMetaData = new MethodMetaData();
			methodMetaData.method = method;
			methodMetaData.fieldName = fieldName;
			return methodMetaData;
		}


	}


}
