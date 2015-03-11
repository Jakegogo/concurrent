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
     * 是否增加数字公共方法
     */
    private boolean addNumberSerializeCommonMethod = false;


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
     * @param curMethodVisitor 
     */
    public MethodVisitor invokeNextSerialize(String name, MethodVisitor curMethodVisitor) {

        if (name == null) {
            name = "default";
        }
        
        String newMethodName = "serialze_" + name + "_" + (methodId ++);

        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, newMethodName, "(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/utils/IdentityHashMap;)V", null, null);
        mv.visitCode();

        curMethodVisitor.visitMethodInsn(INVOKEINTERFACE, AsmUtils.toAsmCls(className), newMethodName, "(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/utils/IdentityHashMap;)V", true);
        
        return mv;
    }

    		
	public ClassWriter getClassWriter() {
		return classWriter;
	}


    public String getClassName() {
        return className;
    }


    public boolean isAddNumberSerializeCommonMethod() {
        return addNumberSerializeCommonMethod;
    }

    public void setAddNumberSerializeCommonMethod(boolean addNumberSerializeCommonMethod) {
        this.addNumberSerializeCommonMethod = addNumberSerializeCommonMethod;
    }


}
