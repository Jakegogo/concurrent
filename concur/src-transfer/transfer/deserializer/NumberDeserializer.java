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
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Number解析器
 * Created by Jake on 2015/2/24.
 */
public class NumberDeserializer implements Deserializer, Opcodes {

    // 0000 0111 数字类型
    public static final byte NUMBER_MASK = (byte) 0x07;

    // 0000 1000
    public static final byte FLAG_NEGATIVE = (byte) 0x08;


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, DeserialContext context) {

		context.nextStackTrace(type);

        byte typeFlag = TransferConfig.getType(flag);
        if (typeFlag != Types.NUMBER) {
            throw new IllegalTypeException(context, typeFlag, Types.NUMBER, type);
        }

        byte extraFlag = TransferConfig.getExtra(flag);
        // 符号
        int sign = (extraFlag & FLAG_NEGATIVE) > 0 ? -1 : 1;
        byte len = (byte) (extraFlag & NUMBER_MASK);

        Number number = null;
        switch (len) {
            case TransferConfig.VARINT:
                number = BitUtils.getInt(inputable) * sign;
                if (type == int.class || type == Integer.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.VARLONG:
                number = BitUtils.getLong(inputable) * sign;
                if (type == long.class || type == Long.class) {
                    return (T) number;
                }
                break;
        }

        if (type == null || type == Number.class) {
            return (T) number;
        }

        if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(number.shortValue());
        } else if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(number.intValue());
        } else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(number.longValue());
        } else  if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(number.intValue() == 1);
        } else if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(number.floatValue());
        } else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(number.doubleValue());
        } else if (type == byte.class || type == Byte.class) {
            return (T) TypeUtils.castToByte(number);
        } else if (type == AtomicInteger.class) {
            return (T) new AtomicInteger(number.intValue());
        } else if (type == AtomicLong.class) {
            return (T) new AtomicLong(number.longValue());
        }

        return (T) number;
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
    	mv.visitIntInsn(BIPUSH, Types.NUMBER);
    	Label l2 = new Label();
    	mv.visitJumpInsn(IF_ICMPEQ, l2);
    	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
    	mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 4);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.NUMBER);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(Ltransfer/core/DeserialContext;BBLjava/lang/reflect/Type;)V", false);
    	mv.visitInsn(ATHROW);
    	mv.visitLabel(l2);
    	
    	mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
    	mv.visitVarInsn(ILOAD, 3);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getExtra", "(B)B", false);
    	mv.visitVarInsn(ISTORE, 6);
    	
    	mv.visitVarInsn(ILOAD, 6);
    	mv.visitIntInsn(BIPUSH, 8);
    	mv.visitInsn(IAND);
    	Label l5 = new Label();
    	mv.visitJumpInsn(IFLE, l5);
    	mv.visitInsn(ICONST_M1);
    	Label l6 = new Label();
    	mv.visitJumpInsn(GOTO, l6);
    	mv.visitLabel(l5);
    	
    	mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
    	mv.visitInsn(ICONST_1);
    	mv.visitLabel(l6);
    	
    	mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
    	mv.visitVarInsn(ISTORE, 7);
    	mv.visitVarInsn(ILOAD, 6);
    	mv.visitIntInsn(BIPUSH, 7);
    	mv.visitInsn(IAND);
    	mv.visitInsn(I2B);
    	mv.visitVarInsn(ISTORE, 8);
    	
    	// Number number = null;
    	mv.visitInsn(ACONST_NULL);
    	mv.visitVarInsn(ASTORE, 9);
    	
    	mv.visitVarInsn(ILOAD, 8);
    	Label l10 = new Label();
    	Label l11 = new Label();
    	Label l12 = new Label();
    	mv.visitTableSwitchInsn(0, 1, l12, new Label[] { l10, l11 });
    	mv.visitLabel(l10);
    	mv.visitFrame(Opcodes.F_APPEND,3, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/Number"}, 0, null);
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
    	mv.visitVarInsn(ILOAD, 7);
    	mv.visitInsn(IMUL);
    	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    	mv.visitVarInsn(ASTORE, 9);
    	
//    	if (type == int.class || type == Integer.class) {
//            return (T) number;
//        }
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
    	Label l14 = new Label();
    	mv.visitJumpInsn(IF_ACMPEQ, l14);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitLdcInsn(org.objectweb.asm.Type.getType("Ljava/lang/Integer;"));
    	mv.visitJumpInsn(IF_ACMPNE, l12);
    	mv.visitLabel(l14);
    	mv.visitVarInsn(ALOAD, 9);
    	mv.visitInsn(ARETURN);
    	
    	mv.visitLabel(l11);
    	
//    	if (type == long.class || type == Long.class) {
//            return (T) number;
//        }
    	mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getLong", "(Ltransfer/Inputable;)J", false);
    	mv.visitVarInsn(ILOAD, 7);
    	mv.visitInsn(I2L);
    	mv.visitInsn(LMUL);
    	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
    	mv.visitVarInsn(ASTORE, 9);
    	
    	
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
    	Label l16 = new Label();
    	mv.visitJumpInsn(IF_ACMPEQ, l16);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitLdcInsn(org.objectweb.asm.Type.getType("Ljava/lang/Long;"));
    	mv.visitJumpInsn(IF_ACMPNE, l12);
    	mv.visitLabel(l16);
    	mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    	mv.visitVarInsn(ALOAD, 9);
    	mv.visitInsn(ARETURN);
    	
    	mv.visitLabel(l12);
    	
    	
    	if (type == null || type == Number.class || type == Object.class) {
    		mv.visitVarInsn(ALOAD, 9);
        	mv.visitInsn(ARETURN);
        	mv.visitMaxs(1, 5);
        	mv.visitEnd();
        	return;
        }
    	
    	
    	if (type == short.class || type == Short.class) {
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "shortValue", "()S", false);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == int.class || type == Integer.class) {
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == long.class || type == Long.class) {
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == float.class || type == Float.class) {
    		mv.visitVarInsn(ALOAD, 9);
    		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
    		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
    		mv.visitInsn(ARETURN);
        } else if (type == double.class || type == Double.class) {
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D", false);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == byte.class || type == Byte.class) {
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/TypeUtils", "castToByte", "(Ljava/lang/Object;)Ljava/lang/Byte;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == boolean.class || type == Boolean.class) {
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
        	mv.visitInsn(ICONST_1);
        	Label l29 = new Label();
        	mv.visitJumpInsn(IF_ICMPNE, l29);
        	mv.visitInsn(ICONST_1);
        	Label l30 = new Label();
        	mv.visitJumpInsn(GOTO, l30);
        	mv.visitLabel(l29);
        	mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        	mv.visitInsn(ICONST_0);
        	mv.visitLabel(l30);
        	mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == AtomicInteger.class) {
        	mv.visitTypeInsn(NEW, "java/util/concurrent/atomic/AtomicInteger");
        	mv.visitInsn(DUP);
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
        	mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/atomic/AtomicInteger", "<init>", "(I)V", false);
        	mv.visitInsn(ARETURN);
        } else if (type == AtomicLong.class) {
        	mv.visitTypeInsn(NEW, "java/util/concurrent/atomic/AtomicLong");
        	mv.visitInsn(DUP);
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
        	mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/atomic/AtomicLong", "<init>", "(J)V", false);
        	mv.visitInsn(ARETURN);
        } else {
        	mv.visitVarInsn(ALOAD, 9);
        	mv.visitInsn(ARETURN);
        }
    	
    	mv.visitMaxs(1, 5);
    	mv.visitEnd();
    	
	}
    

    private static NumberDeserializer instance = new NumberDeserializer();

    public static NumberDeserializer getInstance() {
        return instance;
    }

}
