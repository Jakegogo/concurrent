package dbcache.support.asm;

import dbcache.index.DbIndexService;
import dbcache.support.asm.util.AsmUtils;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 构建构造方法
 * Created by Jake on 2015/2/8.
 */
public class ConstructorBuilder implements Opcodes {

    private Map<Class<?>, ParameterInit> initParams = new LinkedHashMap<Class<?>, ParameterInit>();

    private Map<Class<?>, String> fields = new LinkedHashMap<Class<?>, String>();

    private ClassWriter classWriter;

    private Class<?> originalClass;

    private String enhancedClassName;

    //Constructor
    private Constructor constructor;

    public ConstructorBuilder(ClassWriter classWriter, Class<?> originalClass,
                              String enhancedClassName) {
        this.classWriter = classWriter;
        this.originalClass = originalClass;
        this.enhancedClassName = enhancedClassName;
    }


    public static interface ParameterInit {
        int parameterIndexOfgetProxyEntity();
        void onConstruct(ClassWriter classWriter, MethodVisitor mvInit, Class<?> originalClass, String enhancedClassName, int localIndex);
    }


    public ConstructorBuilder appendParameter(Class<?> paramClass, ParameterInit parameterInit) {
        initParams.put(paramClass, parameterInit);
        return this;
    }

    public ConstructorBuilder appendField(Class<?> fieldClass, String fieldName) {
        fields.put(fieldClass, fieldName);
        return this;
    }

    /**
     * 构建构造方法
     * @return
     */
    public ConstructorBuilder build() {

        // 添加属性
        for (Map.Entry<Class<?>, String> fieldEntry : fields.entrySet()) {
            classWriter.visitField(Opcodes.ACC_PROTECTED, fieldEntry.getValue(),
                    Type.getDescriptor(fieldEntry.getKey()), null, null);
        }

        // 调用originalClassName的<init>方法，否则class不能实例化
        MethodVisitor mvInit = classWriter.visitMethod(ACC_PUBLIC, EntityClassProxyAdapter.INIT, "()V",
                null, null);
        mvInit.visitVarInsn(ALOAD, 0);
        mvInit.visitMethodInsn(INVOKESPECIAL,
                AsmUtils.toAsmCls(originalClass.getName()), EntityClassProxyAdapter.INIT, "()V");
        mvInit.visitInsn(RETURN);
        mvInit.visitMaxs(0, 0);
        mvInit.visitEnd();


        // 添加真实对象和切面处理对象构造方法,用真实类对象作为参数
        String paramsDescriptor = "";
        for (Map.Entry<Class<?>, ParameterInit> initParamEntry : initParams.entrySet()) {
            paramsDescriptor += Type.getDescriptor(initParamEntry.getKey());
        }

        MethodVisitor mvInit2 = classWriter.visitMethod(ACC_PUBLIC, EntityClassProxyAdapter.INIT, "("
                + paramsDescriptor + ")V", null, null);
        mvInit2.visitVarInsn(Opcodes.ALOAD, 0);
        //super.<init>
        mvInit2.visitMethodInsn(INVOKESPECIAL,
                AsmUtils.toAsmCls(originalClass.getName()), EntityClassProxyAdapter.INIT, "()V");

        // onConstruct
        int localIndex = 1;
        for (Map.Entry<Class<?>, ParameterInit> initParamEntry : initParams.entrySet()) {
            initParamEntry.getValue().onConstruct(classWriter, mvInit2, originalClass, enhancedClassName, localIndex ++);
        }

        mvInit2.visitInsn(RETURN);
        mvInit2.visitMaxs(localIndex + 1, localIndex);
        mvInit2.visitEnd();

        return this;
    }


    /**
     * 获取构造方法
     * @param proxyClass 代理类
     * @return
     */
    public Constructor getConstructor(Class<?> proxyClass) {
        if (constructor != null) {
            return constructor;
        }
        Class<?>[] paramTypes = new Class<?>[initParams.size()];
        int index = 0;
        for (Map.Entry<Class<?>, ParameterInit> initParamEntry : initParams.entrySet()) {
            paramTypes[index++] = initParamEntry.getKey();
        }

        Constructor<?> con;
        try {
            con = proxyClass.getConstructor(paramTypes);
            constructor = con;
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取代理对象
     * @param proxyClass 代理类
     * @param constructParams 构造方法的参数
     * @see ParameterInit#parameterIndexOfgetProxyEntity()
     */
    public <T> T getProxyEntity(Class<T> proxyClass, Object... constructParams) {

        Constructor<?> con = getConstructor(proxyClass);
        if (con == null) {
            throw new IllegalStateException("无法获取类[" + proxyClass + "]的构造方法.");
        }

        Object[] params = new Object[initParams.size()];
        int index = 0;
        for (Map.Entry<Class<?>, ParameterInit> initParamEntry : initParams.entrySet()) {
            int paramIndex = initParamEntry.getValue().parameterIndexOfgetProxyEntity();
            params[index++] = constructParams[paramIndex];
        }

        try {
            return (T) con.newInstance(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 创建代理对象
     * @param proxyClass 代理类
     * @param entity 实体 0
     * @param indexService 索引服务 1
     * @param modifiedFields 修改的字段index 2
     * @param <T>
     * @see ParameterInit#parameterIndexOfgetProxyEntity()
     * @return
     */
    public <T> T getProxyEntity(Class<T> proxyClass, T entity, DbIndexService indexService, AtomicIntegerArray modifiedFields) {
        return this.getProxyEntity(proxyClass, entity, indexService, modifiedFields);
    }

    // getters
    public Class<?> getOriginalClass() {
        return originalClass;
    }

    public ClassWriter getClassWriter() {
        return classWriter;
    }

    public String getEnhancedClassName() {
        return enhancedClassName;
    }
}
