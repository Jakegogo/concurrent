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
 * Decimal解析器
 * Created by Jake on 2015/2/24.
 */
public class DecimalDeserializer implements Deserializer, Opcodes {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, DeserialContext context) {

		context.nextStackTrace(type);

        byte typeFlag = TransferConfig.getType(flag);
        if (typeFlag != Types.DECIMAL) {
            throw new IllegalTypeException(context, typeFlag, Types.DECIMAL, type);
        }

        byte extraFlag = TransferConfig.getExtra(flag);
        Number number = null;
        switch (extraFlag) {
            case TransferConfig.FLOAT:
                number = BitUtils.getFloat(inputable);
                if (type == float.class || type == Float.class) {
                    return (T) number;
                }
                break;
            case TransferConfig.DOUBLE:
                number = BitUtils.getDouble(inputable);
                if (type == double.class || type == Double.class) {
                    return (T) number;
                }
                break;
        }

        if (type == null || type == Number.class || type == Object.class) {
            return (T) number;
        }

        if (number == null) {
            if (TypeUtils.getRawClass(type).isPrimitive()) {
                return (T) Integer.valueOf(0);
            }
            return null;
        }

        if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(number.floatValue());
        } else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(number.doubleValue());
        } else if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(number.shortValue());
        } else if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(number.intValue());
        } else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(number.longValue());
        } else if (type == byte.class || type == Byte.class) {
            return (T) TypeUtils.castToByte(number);
        } else if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(number.intValue() == 1);
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
    	mv.visitIntInsn(BIPUSH, Types.DECIMAL);
    	Label l2 = new Label();
    	mv.visitJumpInsn(IF_ICMPEQ, l2);
    	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
    	mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 4);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.DECIMAL);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(Ltransfer/core/DeserialContext;BBLjava/lang/reflect/Type;)V", false);
    	mv.visitInsn(ATHROW);
    	mv.visitLabel(l2);
    	
    	mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
    	mv.visitVarInsn(ILOAD, 3);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getExtra", "(B)B", false);
    	mv.visitVarInsn(ISTORE, 6);
    	mv.visitInsn(ACONST_NULL);
    	mv.visitVarInsn(ASTORE, 7);
    	mv.visitVarInsn(ILOAD, 6);
    	
    	Label l6 = new Label();
    	Label l7 = new Label();
    	Label l8 = new Label();
    	mv.visitTableSwitchInsn(0, 1, l8, new Label[] { l6, l7 });
    	mv.visitLabel(l6);
    	mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, "java/lang/Number"}, 0, null);
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getFloat", "(Ltransfer/Inputable;)F", false);
    	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
    	if (type == float.class || type == Float.class) {
    		mv.visitInsn(ARETURN);
        } else {
        	mv.visitVarInsn(ASTORE, 7);
        }
    	mv.visitJumpInsn(GOTO, l8);
    	mv.visitLabel(l7);
    	mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    	mv.visitVarInsn(ALOAD, 1);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getDouble", "(Ltransfer/Inputable;)D", false);
    	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
    	if (type == double.class || type == Double.class) {
    		mv.visitInsn(ARETURN);
        } else {
        	mv.visitVarInsn(ASTORE, 7);
        }
    	mv.visitLabel(l8);
    	mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    	
    	
    	if (type == null || type == Number.class || type == Object.class) {
    		mv.visitVarInsn(ALOAD, 7);
        	mv.visitInsn(ARETURN);
        	return;
        }

        //TODO
//        if (number == null) {
//            if (TypeUtils.getRawClass(type).isPrimitive()) {
//                return (T) Integer.valueOf(0);
//            }
//            return null;
//        }
    	
    	if (type == float.class || type == Float.class) {
    		mv.visitVarInsn(ALOAD, 7);
    		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
    		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
    		mv.visitInsn(ARETURN);
        } else if (type == double.class || type == Double.class) {
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D", false);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == short.class || type == Short.class) {
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "shortValue", "()S", false);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == int.class || type == Integer.class) {
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == long.class || type == Long.class) {
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == byte.class || type == Byte.class) {
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/TypeUtils", "castToByte", "(Ljava/lang/Object;)Ljava/lang/Byte;", false);
        	mv.visitInsn(ARETURN);
        } else if (type == boolean.class || type == Boolean.class) {
        	mv.visitVarInsn(ALOAD, 7);
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
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
        	mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/atomic/AtomicInteger", "<init>", "(I)V", false);
        	mv.visitInsn(ARETURN);
        } else if (type == AtomicLong.class) {
        	mv.visitTypeInsn(NEW, "java/util/concurrent/atomic/AtomicLong");
        	mv.visitInsn(DUP);
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
        	mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/atomic/AtomicLong", "<init>", "(J)V", false);
        	mv.visitInsn(ARETURN);
        } else {
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitInsn(ARETURN);
        }
    	
    	mv.visitMaxs(1, 5);
    	mv.visitEnd();
	}

    private static final DecimalDeserializer instance = new DecimalDeserializer();

    public static DecimalDeserializer getInstance() {
        return instance;
    }

}
