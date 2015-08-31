package transfer.compile;

import utils.enhance.asm.util.AsmUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * 预编译编码器上下文
 * Created by Jake on 2015/3/8.
 */
public class AsmSerializerContext implements Opcodes {

    /**
     * 类名
     */
    private final String className;

    /**
     * ClassWriter
     */
    private final ClassWriter classWriter;

    /**
     * 自增的方法Id
     */
    private int methodId = 1;


    /**
     * 构造方法
     * @param className
     * @param classWriter
     */
    public AsmSerializerContext(String className, ClassWriter classWriter) {
        this.className = className;
        this.classWriter = classWriter;
    }


    /**
     * 执行下一个编码方法
     * @return
     * @param name
     * @param curMethodVisitor 
     */
    public MethodVisitor invokeNextSerialize(String name, MethodVisitor curMethodVisitor) {
        if (name == null) {
            name = "default";
        }
        
        String newMethodName = "serialze_" + name + "_" + (methodId ++);
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, newMethodName, "(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/core/SerialContext;)V", null, null);
        mv.visitCode();

        curMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, AsmUtils.toAsmCls(className), newMethodName, "(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/core/SerialContext;)V", false);
        return mv;
    }

	public ClassWriter getClassWriter() {
		return classWriter;
	}

    public String getClassName() {
        return className;
    }


}
