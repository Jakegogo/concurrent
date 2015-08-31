package transfer.compile;

import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;
import transfer.deserializer.Deserializer;
import transfer.exceptions.CompileError;
import transfer.utils.TypeUtils;
import utils.enhance.asm.AsmClassLoader;
import utils.enhance.asm.util.AsmUtils;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * java bean解码器预编译生成工厂
 * @author Jake
 *
 */
public class AsmDeserializerFactory implements Opcodes {
	
	 /**
     * 字节码类加载器
     */
    public static final AsmClassLoader classLoader = new AsmClassLoader();

    /**
     * 预编译解码器的自增ID
     */
    private static final AtomicInteger DESERIALIZER_ID_GENERATOR = new AtomicInteger(0);
    
    
    /**
     * Asm预编译解码器
     * @param type 类型
     * @param outerDeserializer 最外层解码器
     * @return
     */
    public static Deserializer compileDeserializer(Type type, Deserializer outerDeserializer) {
    	
    	String asmClassName = TypeUtils.getRawClass(type).getName() + "_Deserializer_" + DESERIALIZER_ID_GENERATOR.incrementAndGet();
        byte[] bytes = createDeserializerClassBytes(asmClassName, type, outerDeserializer);

        AsmUtils.writeClazz(asmClassName, bytes);
        
        Class<?> serializerClass;
        try {
        	serializerClass = classLoader.defineClass(
                    asmClassName, bytes);
            return (Deserializer) serializerClass.newInstance();
        } catch (Exception e) {
        	
            try {
            	asmClassName = TypeUtils.getRawClass(type).getSimpleName() + "_Deserializer_" + DESERIALIZER_ID_GENERATOR.incrementAndGet();
            	bytes = createDeserializerClassBytes(asmClassName, type, outerDeserializer);
            	
            	serializerClass = classLoader.defineClass(
                        asmClassName, bytes);
            	 return (Deserializer) serializerClass.newInstance();
            } catch (Exception e1) {
            	e.printStackTrace();
            	throw new CompileError(e);
            }
            
        }
    }

    
    /**
     * 构造解码器字节码
     * @param className 解码器asm类名
     * @param type 解码类型
     * @param outerDeserializer 最外层解码器
     * @return
     */
	private static byte[] createDeserializerClassBytes(String className,
			Type type, Deserializer outerDeserializer) {
		ClassWriter cwr = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassVisitor cw = new CheckClassAdapter(cwr, false);
		MethodVisitor mv;

		cw.visit(V1_6, ACC_PUBLIC, AsmUtils.toAsmCls(className), null, "java/lang/Object", new String[] { "transfer/deserializer/Deserializer" });

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

		{
			mv = cw.visitMethod(ACC_PUBLIC, "deserialze", "(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/core/DeserialContext;)Ljava/lang/Object;", "<T:Ljava/lang/Object;>(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/core/DeserialContext;)TT;", null);
			outerDeserializer.compile(type, mv, new AsmDeserializerContext(className, cwr));
		}

		{
			mv = cw.visitMethod(ACC_PUBLIC, "compile", "(Ljava/lang/reflect/Type;Lorg/objectweb/asm/MethodVisitor;" + org.objectweb.asm.Type.getDescriptor(AsmDeserializerContext.class) + ")V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitMaxs(0, 4);
			mv.visitEnd();
		}
		
		cw.visitEnd();

		return cwr.toByteArray();
	}

	
	
}
