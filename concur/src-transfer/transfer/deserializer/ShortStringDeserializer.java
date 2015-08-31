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
import java.nio.charset.Charset;

/**
 * 短字符串解析器 最大长度255
 * Created by Jake on 2015/2/25.
 */
public class ShortStringDeserializer implements Deserializer, Opcodes {

	// 默认编码
    private static final String CHARSET = "UTF-8";
	

    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, DeserialContext context) {

		context.nextStackTrace(type);

        byte typeFlag = TransferConfig.getType(flag);
        if (typeFlag != Types.STRING) {
            throw new IllegalTypeException(context, typeFlag, Types.STRING, type);
        }

        // 读取字符串字节数组的大小
        int length = BitUtils.getInt(inputable);
        byte[] bytes = new byte[length];
        inputable.getBytes(bytes);
        return (T) new String(bytes, Charset.forName("UTF-8"));
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
    	mv.visitIntInsn(BIPUSH, Types.STRING);
    	
    	Label l2 = new Label();
    	mv.visitJumpInsn(IF_ICMPEQ, l2);
    	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
    	mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 4);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.STRING);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(Ltransfer/core/DeserialContext;BBLjava/lang/reflect/Type;)V", false);
    	mv.visitInsn(ATHROW);
    	mv.visitLabel(l2);
    	
    	mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
    	mv.visitVarInsn(ISTORE, 6);
    	mv.visitVarInsn(ILOAD, 6);
    	mv.visitIntInsn(NEWARRAY, T_BYTE);
    	mv.visitVarInsn(ASTORE, 7);
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitVarInsn(ALOAD, 7);
    	
    	mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getBytes", "([B)V", true);
    	mv.visitTypeInsn(NEW, "java/lang/String");
    	mv.visitInsn(DUP);
    	mv.visitVarInsn(ALOAD, 7);
    	mv.visitLdcInsn(CHARSET);
    	mv.visitMethodInsn(INVOKESTATIC, "java/nio/charset/Charset", "forName", "(Ljava/lang/String;)Ljava/nio/charset/Charset;", false);
    	mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V", false);
    	mv.visitInsn(ARETURN);
    	mv.visitMaxs(5, 8);
    	mv.visitEnd();
    	
	}

    private static final ShortStringDeserializer instance = new ShortStringDeserializer();

    public static ShortStringDeserializer getInstance() {
        return instance;
    }

}
