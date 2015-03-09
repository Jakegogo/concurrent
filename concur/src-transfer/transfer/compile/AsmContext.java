package transfer.compile;

import dbcache.support.asm.util.AsmUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * 预编译上下文
 * Created by Jake on 2015/3/8.
 */
public class AsmContext implements Opcodes {

    /**
     * 类名
     */
    private String className;

    /**
     * ClassWriter
     */
    private ClassWriter classWriter;

    /**
     * 自增的方法Id
     */
    private int methodId = 1;


    /**
     * 构造方法
     * @param className
     * @param classWriter
     */
    public AsmContext(String className, ClassWriter classWriter) {
        this.className = className;
        this.classWriter = classWriter;
    }


    /**
     * 执行下一个编码方法
     * @return
     * @param name
     */
    public MethodVisitor invokeNextSerialize(String name) {

        if (name == null) {
            name = "default";
        }

        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "serialze_" + name + "_" + (methodId ++), "(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/utils/IdentityHashMap;)V", null, null);

        mv.visitMethodInsn(INVOKEVIRTUAL, AsmUtils.toAsmCls(className), "serialze", "(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/utils/IdentityHashMap;)V", true);

        return mv;
    }








}
