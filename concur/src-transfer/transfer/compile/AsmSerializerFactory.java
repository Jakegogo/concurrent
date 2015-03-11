package transfer.compile;

import dbcache.support.asm.AsmClassLoader;
import dbcache.support.asm.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import transfer.exception.CompileError;
import transfer.serializer.Serializer;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * java bean编码器预编译生成工厂
 * Created by Jake on 2015/3/7.
 */
public class AsmSerializerFactory implements Opcodes {


    /**
     * 字节码类加载器
     */
    public static AsmClassLoader classLoader = new AsmClassLoader();

    /**
     * asm动态生成编码器ID自增
     */
    private static AtomicInteger SERIALIZER_ID_GENERATOR = new AtomicInteger(0);


    /**
     * Asm预编译编码器
     * @param type 类型
     * @param outerSerializer 最外层编码器
     * @return
     */
    public static Serializer compileSerializer(Type type, Serializer outerSerializer) {

        String asmClassName = TypeUtils.getRawClass(type).getName() + "_" + SERIALIZER_ID_GENERATOR.incrementAndGet();

        byte[] bytes = createSerializerClassBytes(asmClassName, type, outerSerializer);

        AsmUtils.writeClazz(asmClassName, bytes);
        
        Class<?> serializerClass = (Class<?>) classLoader.defineClass(
                asmClassName, bytes);

        try {
            return (Serializer) serializerClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new CompileError(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new CompileError(e);
        }

    }



    /**
     * 构造编码器字节码
     * @param className 编码器asm类名
     * @param type 编码类型
     * @param outerSerializer 最外层编码器
     * @return
     */
    private static byte[] createSerializerClassBytes(String className, Type type, Serializer outerSerializer) {

        ClassWriter cwr = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor cw = new CheckClassAdapter(cwr, false);
        MethodVisitor mv;

        cw.visit(V1_6, ACC_PUBLIC, AsmUtils.toAsmCls(className), null, "java/lang/Object", new String[] { "transfer/serializer/Serializer" });

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "serialze", "(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/utils/IdentityHashMap;)V", null, null);

            outerSerializer.compile(type, mv, new AsmContext(className, cwr));

        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "compile", "(Ljava/lang/reflect/Type;Lorg/objectweb/asm/MethodVisitor;Ltransfer/compile/AsmContext;)V", null, null);
            mv.visitCode();
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 4);
            mv.visitEnd();
        }

        cw.visitEnd();

        return cwr.toByteArray();
    }




}
