package dbcache.support.asm;

import dbcache.service.DbIndexService;
import dbcache.utils.AsmUtils;
import dbcache.utils.MutableInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认方法切面替换处理
 * @author Jake
 * @date 2014年9月7日下午6:51:36
 */
@Component
public class IndexMethodReplaceAspect extends AbstractAsmMethodReplaceAspect {


	/**
	 * 实体索引服务
	 */
	@Autowired
	private DbIndexService<?> entityIndexService;

	/**
	 * 索引信息缓存
	 * 实体类 - 索引信息
	 */
	private ConcurrentHashMap<Class<?>, ClassIndexesMetaData> CLASS_INDEX_INFO = new ConcurrentHashMap<Class<?>, ClassIndexesMetaData>();



	@Override
	public void doInitClass(ClassWriter classWriter, Class<?> originalClass,
			String enhancedClassName) {
		
		// 增加切面处理对象
		classWriter.visitField(Opcodes.ACC_PROTECTED, EntityClassReplaceAdapter.HANDLER_OBJECT,
				Type.getDescriptor(getAspectHandleClass()), null, null);

		// 调用originalClassName的默认<init>方法，否则class不能实例化
		MethodVisitor mvInit = classWriter.visitMethod(ACC_PUBLIC, EntityClassReplaceAdapter.INIT, "()V",
				null, null);
		mvInit.visitVarInsn(ALOAD, 0);
		mvInit.visitMethodInsn(INVOKESPECIAL,
				AsmUtils.toAsmCls(originalClass.getName()), EntityClassReplaceAdapter.INIT, "()V");
		mvInit.visitInsn(RETURN);
		mvInit.visitMaxs(0, 0);
		mvInit.visitEnd();


		// 添加构造方法,用HANDLER_OBJECT作为参数
		MethodVisitor mvInit2 = classWriter.visitMethod(ACC_PUBLIC, EntityClassReplaceAdapter.INIT, "("
				+ Type.getDescriptor(getAspectHandleClass()) + ")V", null, null);
		mvInit2.visitVarInsn(Opcodes.ALOAD, 0);

		mvInit2.visitMethodInsn(INVOKESPECIAL,
				AsmUtils.toAsmCls(originalClass.getName()), EntityClassReplaceAdapter.INIT, "()V");

		mvInit2.visitVarInsn(Opcodes.ALOAD, 0);
		mvInit2.visitVarInsn(Opcodes.ALOAD, 1);

		mvInit2.visitFieldInsn(Opcodes.PUTFIELD,
				AsmUtils.toAsmCls(enhancedClassName), EntityClassReplaceAdapter.HANDLER_OBJECT,
				Type.getDescriptor(getAspectHandleClass()));

		mvInit2.visitInsn(RETURN);
		mvInit2.visitMaxs(2, 2);
		mvInit2.visitEnd();
				
	}

	@Override
	public void initClassMetaInfo(final Class<?> clazz, final String enhancedClassName) {

		final ClassIndexesMetaData indexesMetaData = new ClassIndexesMetaData();
		final Map<String, Set<MethodMetaData>> methodsMap = indexesMetaData.changeIndexValueMethods;
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
		//存储到缓存
		CLASS_INDEX_INFO.putIfAbsent(clazz, indexesMetaData);

	}

	//获取索引对应的方法集合
	private Set<MethodMetaData> getIndexNameSet(Map<String, Set<MethodMetaData>> methodsMap, Method method) {
		Set<MethodMetaData> methodMetaData = methodsMap.get(method.getName());
		if(methodMetaData == null) {
			methodMetaData = new TreeSet<MethodMetaData>();
			methodsMap.put(method.getName(), methodMetaData);
		}
		return methodMetaData;
	}


	@Override
	public MethodVisitor doBefore(final Class<?> entityClass, MethodVisitor mWriter, Method method, int locals, String name, int acc, String desc) {
		//获取类信息
		final ClassIndexesMetaData classIndexesMetaData = CLASS_INDEX_INFO.get(entityClass);
		if(classIndexesMetaData == null) {
			return mWriter;
		}
		//获取需要拦截的方法列表
		final Map<String, Set<MethodMetaData>> methodsMap = classIndexesMetaData.changeIndexValueMethods;
		if(!methodsMap.containsKey(name)) {
			return mWriter;
		}
		final Set<MethodMetaData> methodMetaDatas = methodsMap.get(name);
		if (methodMetaDatas.size() == 0) {
			return mWriter;
		}
		
		//获取索引属性
		final Map<String, Field> fieldsMap = classIndexesMetaData.indexFields;	
		
		for (MethodMetaData methodMetaData : methodMetaDatas) {
			method = methodMetaData.method;
			break;
		}
		//统计当前maxLocals
		int i = 1;
		// 遍历方法的所有参数
		for (Class<?> tCls : method.getParameterTypes()) {
			Type t = Type.getType(tCls);
			i++;
			// long和double 用64位表示，要后移一个位置，否则会报错
			if (t.getSort() == Type.LONG || t.getSort() == Type.DOUBLE) {
				i++;
			}
		}
		locals = i;
		
		// 计数器
    	final MutableInteger localsCounter = new MutableInteger(locals);
		
		// 添加方法体前调用
		AdviceAdapter adviceAdapter = new AdviceAdapter(Opcodes.ASM4, mWriter, acc, desc, desc) {

			@Override
			protected void onMethodEnter() {
				
				for (MethodMetaData methodMetaData : methodMetaDatas) {
					
					//获取属性
					final Field field = fieldsMap.get(methodMetaData.indexName);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
		
					PropertyDescriptor propertyDescriptor = null;
					try {
						propertyDescriptor = new PropertyDescriptor(field.getName(), entityClass);
					} catch (IntrospectionException e) {
						e.printStackTrace();
						return;
					}
					Method getMethod = propertyDescriptor.getReadMethod();
		
					Type mt = Type.getType(getMethod);
					mv.visitMethodInsn(INVOKEVIRTUAL,
							AsmUtils.toAsmCls(field.getDeclaringClass().getName()), getMethod.getName(),
							mt.toString());
		
					// 处理返回值类型 到 Object类型
					Type rt = Type.getReturnType(getMethod);
					AsmUtils.withBoxingType(mv, rt);
		
					//存储到变量
					mv.visitVarInsn(Opcodes.ASTORE, localsCounter.get());
		
					methodMetaData.local = localsCounter.get();
		
					localsCounter.incrementAndGet();
				}

			}
			
			
			@Override
			protected void onMethodExit(int opcode) {

				for (MethodMetaData methodMetaData : methodMetaDatas) {
					
					localsCounter.incrementAndGet();
					//获取属性
					final Field field = fieldsMap.get(methodMetaData.indexName);
					//获取this.getFieldName
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					
					PropertyDescriptor propertyDescriptor = null;
					try {
						propertyDescriptor = new PropertyDescriptor(field.getName(), entityClass);
					} catch (IntrospectionException e) {
						e.printStackTrace();
						return;
					}
					Method getMethod = propertyDescriptor.getReadMethod();
		
					Type mt = Type.getType(getMethod);
					mv.visitMethodInsn(INVOKEVIRTUAL,
							AsmUtils.toAsmCls(field.getDeclaringClass().getName()), getMethod.getName(),
							mt.toString());
					
					// 处理返回值类型  到 Object类型
					Type rt = Type.getReturnType(getMethod);
					AsmUtils.withBoxingType(mv, rt);
		
					//存储到变量
					mv.visitVarInsn(Opcodes.ASTORE, localsCounter.get());
		
					//调用 changeIndex(Object entity, String indexName, Object oldValue, Object newValue)
					//获取this.handler
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitFieldInsn(Opcodes.GETFIELD, AsmUtils.toAsmCls(classIndexesMetaData.enhancedClassName), EntityClassReplaceAdapter.HANDLER_OBJECT, Type.getDescriptor(getAspectHandleClass()));
		
					//获取this
					mv.visitVarInsn(Opcodes.ALOAD, 0);
		
					//load indexName
					mv.visitLdcInsn(methodMetaData.indexName);
		
					//获取属性修改前的值
					mv.visitVarInsn(Opcodes.ALOAD, methodMetaData.local);
		
					//获取属性修改后的值
					mv.visitVarInsn(Opcodes.ALOAD, localsCounter.get());
		
					//调用静态方法
					mv.visitMethodInsn(INVOKEINTERFACE, AsmUtils.toAsmCls(getAspectHandleClass().getName()), "update", "(Ldbcache/model/IEntity;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
		
				}
		
			}
			
			
		};

		return adviceAdapter;
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
		 * 方法名 - 索引信息
		 */
		Map<String, Set<MethodMetaData>> changeIndexValueMethods = new HashMap<String, Set<MethodMetaData>>();

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


}
