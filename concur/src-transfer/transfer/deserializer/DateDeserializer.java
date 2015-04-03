package transfer.deserializer;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;
import java.util.Date;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 日期解析器
 * Created by Jake on 2015/2/25.
 */
public class DateDeserializer implements Deserializer, Opcodes {


    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.DATE_TIME) {
            throw new IllegalTypeException(typeFlag, Types.DATE_TIME, type);
        }

        long timestamp = BitUtils.getLong(inputable);

        return (T) new Date(timestamp);
    }

    
    @Override
	public void compile(Type type, MethodVisitor mv,
			AsmDeserializerContext context) {
    	
    	mv.visitCode();
    	mv.visitVarInsn(ILOAD, 3);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getType", "(B)B", false);
    	mv.visitVarInsn(ISTORE, 5);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.DATE_TIME);
    	
    	Label l2 = new Label();
    	mv.visitJumpInsn(IF_ICMPEQ, l2);
    	mv.visitTypeInsn(NEW, "transfer/exception/IllegalTypeException");
    	mv.visitInsn(DUP);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.DATE_TIME);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exception/IllegalTypeException", "<init>", "(BBLjava/lang/reflect/Type;)V", false);
    	mv.visitInsn(ATHROW);
    	mv.visitLabel(l2);
    	
    	mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getLong", "(Ltransfer/Inputable;)J", false);
    	mv.visitVarInsn(LSTORE, 6);
    	mv.visitTypeInsn(NEW, "java/util/Date");
    	mv.visitInsn(DUP);
    	mv.visitVarInsn(LLOAD, 6);
    	mv.visitMethodInsn(INVOKESPECIAL, "java/util/Date", "<init>", "(J)V", false);
    	
    	mv.visitInsn(ARETURN);
    	mv.visitMaxs(5, 8);
    	mv.visitEnd();
    	
	}

    private static DateDeserializer instance = new DateDeserializer();

    public static DateDeserializer getInstance() {
        return instance;
    }

}
