package transfer.deserializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.DeserialContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exceptions.IllegalTypeException;

import java.lang.reflect.Type;

/**
 * 布尔解析器
 * Created by Jake on 2015/2/25.
 */
public class BooleanDeserializer implements Deserializer, Opcodes {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, DeserialContext context) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.BOOLEAN) {
            throw new IllegalTypeException(context, typeFlag, Types.BOOLEAN, type);
        }

        byte extraFlag = TransferConfig.getExtra(flag);
        if (extraFlag == 0x01) {
            return (T) Boolean.valueOf(true);
        }

        return (T) Boolean.valueOf(false);
    }

    
    @Override
	public void compile(Type type, MethodVisitor mv,
			AsmDeserializerContext context) {
    	
    	mv.visitCode();
    	
//      if (flag == Types.NULL) {
//   		return null;
//  	}
	    mv.visitVarInsn(ILOAD, 3);
	    mv.visitInsn(ICONST_1);
	    Label l1 = new Label();
	    mv.visitJumpInsn(IF_ICMPNE, l1);
	    mv.visitInsn(ACONST_NULL);
	    mv.visitInsn(ARETURN);
	    mv.visitLabel(l1);
    	
    	mv.visitVarInsn(ILOAD, 3);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getType", "(B)B", false);
    	mv.visitVarInsn(ISTORE, 5);
    	
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.BOOLEAN);
    	
    	Label l2 = new Label();
    	mv.visitJumpInsn(IF_ICMPEQ, l2);
    	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
    	mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 4);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.BOOLEAN);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(Ltransfer/core/DeserialContext;BBLjava/lang/reflect/Type;)V", false);
    	mv.visitInsn(ATHROW);
    	mv.visitLabel(l2);
    	
    	mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
    	mv.visitVarInsn(ILOAD, 3);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getExtra", "(B)B", false);
    	mv.visitVarInsn(ISTORE, 6);
    	
    	mv.visitVarInsn(ILOAD, 6);
    	mv.visitInsn(ICONST_1);
    	Label l5 = new Label();
    	mv.visitJumpInsn(IF_ICMPNE, l5);
    	mv.visitInsn(ICONST_1);
    	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
    	mv.visitInsn(ARETURN);
    	mv.visitLabel(l5);
    	
    	mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
    	mv.visitInsn(ICONST_0);
    	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
    	
    	mv.visitInsn(ARETURN);
    	mv.visitMaxs(5, 7);
    	mv.visitEnd();
    	
	}

    private static BooleanDeserializer instance = new BooleanDeserializer();

    public static BooleanDeserializer getInstance() {
        return instance;
    }

}
