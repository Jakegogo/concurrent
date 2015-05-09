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
import transfer.utils.BitUtils;

import java.lang.reflect.Type;

/**
 * 字节数组解析器
 * Created by Jake on 2015/2/24.
 */
public class ByteArrayWrappDeSerializer implements Deserializer, Opcodes {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, DeserialContext context) {

        byte typeFlag = TransferConfig.getType(flag);
        if (typeFlag != Types.BYTE_ARRAY) {
            throw new IllegalTypeException(context, typeFlag, Types.BYTE_ARRAY, type);
        }

        // 读取字节数组的大小
        int length = BitUtils.getInt(inputable);
        return (T) inputable.getByteArray(length);
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
    	mv.visitIntInsn(BIPUSH, Types.BYTE_ARRAY);
    	
    	Label l2 = new Label();
    	mv.visitJumpInsn(IF_ICMPEQ, l2);
    	Label l3 = new Label();
    	mv.visitLabel(l3);
    	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
    	mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 4);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.BYTE_ARRAY);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(Ltransfer/core/DeserialContext;BBLjava/lang/reflect/Type;)V", false);
    	mv.visitInsn(ATHROW);
    	mv.visitLabel(l2);
    	
    	mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
    	mv.visitVarInsn(ISTORE, 6);


    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitVarInsn(ILOAD, 6);
    	mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByteArray", "(I)Ltransfer/ByteArray;", true);
    	mv.visitInsn(ARETURN);

    	mv.visitMaxs(5, 7);
    	mv.visitEnd();
    	
	}

    private static ByteArrayWrappDeSerializer instance = new ByteArrayWrappDeSerializer();

    public static ByteArrayWrappDeSerializer getInstance() {
        return instance;
    }

}
