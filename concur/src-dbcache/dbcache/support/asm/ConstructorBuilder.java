package dbcache.support.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import utils.enhance.asm.util.AsmUtils;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * 构建构造方法
 * Created by Jake on 2015/2/8.
 */
public class ConstructorBuilder implements Opcodes {

    private List<ParameterInit> initParams = new ArrayList<ParameterInit>();

    private Map<Class<?>, String> fields = new LinkedHashMap<Class<?>, String>();

    private ClassWriter classWriter;

    private Class<?> originalClass;

    private String enhancedClassName;

    //Constructor
    private Constructor constructor;

    private int initParamSize = 0;

    public ConstructorBuilder(ClassWriter classWriter, Class<?> originalClass,
                              String enhancedClassName) {
        this.classWriter = classWriter;
        this.originalClass = originalClass;
        this.enhancedClassName = enhancedClassName;
    }


    public static abstract class ParameterInit implements Comparable<ParameterInit> {
        abstract Class<?> parameterType();
        abstract int parameterIndexOfgetProxyEntity();
        abstract void onConstruct(
                ClassWriter classWriter,
                MethodVisitor mvInit,
                Class<?> originalClass,
                String enhancedClassName,
                int localIndex);

        @Override
        public int compareTo(ParameterInit o) {
            if (o == this) {
                return 0;
            }
            if (o == null) {
                return -1;
            }
            return this.parameterIndexOfgetProxyEntity() - o.parameterIndexOfgetProxyEntity();
        }
    }


    public ConstructorBuilder appendParameter(ParameterInit parameterInit) {
        initParams.add(parameterInit);
        if (parameterInit.parameterIndexOfgetProxyEntity() >= 0) {
            initParamSize++;
        }
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
        Collections.sort(initParams);
        String paramsDescriptor = "";
        for (ParameterInit initParamEntry : initParams) {
            if (initParamEntry.parameterIndexOfgetProxyEntity() < 0) {
                continue;
            }
            paramsDescriptor += Type.getDescriptor(initParamEntry.parameterType());
        }

        MethodVisitor mvInit2 = classWriter.visitMethod(ACC_PUBLIC, EntityClassProxyAdapter.INIT, "("
                + paramsDescriptor + ")V", null, null);
        mvInit2.visitVarInsn(Opcodes.ALOAD, 0);
        //super.<init>
        mvInit2.visitMethodInsn(INVOKESPECIAL,
                AsmUtils.toAsmCls(originalClass.getName()), EntityClassProxyAdapter.INIT, "()V");

        // onConstruct
        int localIndex = 1;
        for (ParameterInit initParamEntry : initParams) {
            initParamEntry.onConstruct(classWriter, mvInit2, originalClass, enhancedClassName, localIndex);
            if (initParamEntry.parameterIndexOfgetProxyEntity() >= 0) {
                localIndex ++;
            }
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
        Class<?>[] paramTypes = new Class<?>[initParamSize];
        int index = 0;
        for (ParameterInit initParamEntry : initParams) {
            if (initParamEntry.parameterIndexOfgetProxyEntity() < 0) {
                continue;
            }
            paramTypes[index++] = initParamEntry.parameterType();
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

        Object[] params = new Object[initParamSize];
        int index = 0;
        for (ParameterInit initParamEntry : initParams) {
            if (initParamEntry.parameterIndexOfgetProxyEntity() < 0) {
                continue;
            }
            int paramIndex = initParamEntry.parameterIndexOfgetProxyEntity();
            params[index++] = constructParams[paramIndex];
        }

        try {
            return (T) con.newInstance(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
