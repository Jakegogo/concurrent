package transfer.serializer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Outputable;
import transfer.compile.AsmContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.CompileError;
import transfer.utils.IdentityHashMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数字编码器
 * Created by Jake on 2015/2/26.
 */
public class NumberSerializer implements Serializer, Opcodes {

    // 1000 0000
    public static final int FLAG_0X80 = 0x80;

    // 0000 1000
    public static final byte FLAG_NEGATIVE = (byte) 0x08;

    // 0000 0000
    public static final byte FLAG_NOT_NEGATIVE = (byte) 0x00;

    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        Number number = (Number) object;

        if (object instanceof Integer
                || object instanceof Short
                || object instanceof Byte
                || object instanceof AtomicInteger) {

            this.putIntVal(outputable, number);

        } else if(object instanceof Long
                || object instanceof AtomicLong
                || object instanceof BigInteger) {

            this.putLongVal(outputable, number);

        } else if (object instanceof Float || object instanceof Double
                || object instanceof BigDecimal) {

            DecimalSerializer.getInstance().serialze(outputable, object, referenceMap);
        }

    }
    
    
    private void putIntVal(Outputable outputable, Number number) {

        int value = number.intValue();

        byte sign = FLAG_NOT_NEGATIVE;
        if (value < 0) {
            value = -value;
            sign = FLAG_NEGATIVE;
        }

        if (value < FLAG_0X80) {

            outputable.putByte((byte) (Types.NUMBER | sign | TransferConfig.INT1), (byte) value);
        } else {

            byte[] bytes = new byte[4];

            int i = 4;
            while (value > 0) {
                bytes[--i] = (byte) value;
                value >>= 8;
            }

            outputable.putByte((byte) (Types.NUMBER | sign | (3 - i)));
            outputable.putBytes(bytes, i, 4 - i);
        }

	}


    private void putLongVal(Outputable outputable, Number number) {

        long value = number.longValue();

        byte sign = FLAG_NOT_NEGATIVE;
        if (value < 0) {
            value = -value;
            sign = FLAG_NEGATIVE;
        }

        if (value < FLAG_0X80) {

            outputable.putByte((byte) (Types.NUMBER | sign | TransferConfig.INT1), (byte) value);
        } else {

            byte[] bytes = new byte[8];

            int i = 8;
            while (value > 0) {
                bytes[--i] = (byte) value;
                value >>= 8;
            }

            outputable.putByte((byte) (Types.NUMBER | sign | (7 - i)));
            outputable.putBytes(bytes, i, 8 - i);
        }

    }
    

    @Override
    public void compile(Type type, MethodVisitor mv, AsmContext context) {
    	
    	this.addPutValMethods(mv, context.getClassWriter());
    	
    	mv.visitCode();
        mv.visitVarInsn(ALOAD, 2);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNONNULL, l1);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);

        mv.visitInsn(RETURN);
        mv.visitLabel(l1);

        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        
        
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
        mv.visitVarInsn(ASTORE, 4);
        
        Class<?> numberClass = TypeUtils.getRawClass(type);
        if (numberClass == null || numberClass == Object.class) {
        	throw new CompileError("浮点类型不确定.(" + type + ")");
		} else if (numberClass == int.class || numberClass == Integer.class
				|| numberClass == short.class || numberClass == Short.class
				|| numberClass == byte.class || numberClass == Byte.class
				|| numberClass == AtomicInteger.class) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKESPECIAL, "transfer/serializer/NumberSerializer", "putIntVal", "(Ltransfer/Outputable;Ljava/lang/Number;)V", false);
		}
    	
    	
    }


    private void addPutValMethods(MethodVisitor mv, ClassWriter cw) {
    	{
		mv = cw.visitMethod(ACC_PRIVATE, "putIntVal", "(Ltransfer/Outputable;Ljava/lang/Number;)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
		mv.visitVarInsn(ISTORE, 3);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 4);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitVarInsn(ILOAD, 3);
		Label l3 = new Label();
		mv.visitJumpInsn(IFGE, l3);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitVarInsn(ILOAD, 3);
		mv.visitInsn(INEG);
		mv.visitVarInsn(ISTORE, 3);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitIntInsn(BIPUSH, 8);
		mv.visitVarInsn(ISTORE, 4);
		mv.visitLabel(l3);
		mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
		mv.visitVarInsn(ILOAD, 3);
		mv.visitIntInsn(SIPUSH, 128);
		Label l6 = new Label();
		mv.visitJumpInsn(IF_ICMPGE, l6);
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_2);
		mv.visitIntInsn(NEWARRAY, T_BYTE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_0);
		mv.visitIntInsn(BIPUSH, 16);
		mv.visitVarInsn(ILOAD, 4);
		mv.visitInsn(IOR);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IOR);
		mv.visitInsn(I2B);
		mv.visitInsn(BASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_1);
		mv.visitVarInsn(ILOAD, 3);
		mv.visitInsn(I2B);
		mv.visitInsn(BASTORE);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "([B)V", true);
		Label l8 = new Label();
		mv.visitLabel(l8);
		Label l9 = new Label();
		mv.visitJumpInsn(GOTO, l9);
		mv.visitLabel(l6);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(ICONST_4);
		mv.visitIntInsn(NEWARRAY, T_BYTE);
		mv.visitVarInsn(ASTORE, 5);
		Label l10 = new Label();
		mv.visitLabel(l10);
		mv.visitInsn(ICONST_4);
		mv.visitVarInsn(ISTORE, 6);
		Label l11 = new Label();
		mv.visitLabel(l11);
		Label l12 = new Label();
		mv.visitJumpInsn(GOTO, l12);
		Label l13 = new Label();
		mv.visitLabel(l13);
		mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"[B", Opcodes.INTEGER}, 0, null);
		mv.visitVarInsn(ALOAD, 5);
		mv.visitIincInsn(6, -1);
		mv.visitVarInsn(ILOAD, 6);
		mv.visitVarInsn(ILOAD, 3);
		mv.visitInsn(I2B);
		mv.visitInsn(BASTORE);
		Label l14 = new Label();
		mv.visitLabel(l14);
		mv.visitVarInsn(ILOAD, 3);
		mv.visitIntInsn(BIPUSH, 8);
		mv.visitInsn(ISHR);
		mv.visitVarInsn(ISTORE, 3);
		mv.visitLabel(l12);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ILOAD, 3);
		mv.visitJumpInsn(IFGT, l13);
		Label l15 = new Label();
		mv.visitLabel(l15);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitIntInsn(BIPUSH, 16);
		mv.visitVarInsn(ILOAD, 4);
		mv.visitInsn(IOR);
		mv.visitInsn(ICONST_3);
		mv.visitVarInsn(ILOAD, 6);
		mv.visitInsn(ISUB);
		mv.visitInsn(IOR);
		mv.visitInsn(I2B);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);
		Label l16 = new Label();
		mv.visitLabel(l16);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 5);
		mv.visitVarInsn(ILOAD, 6);
		mv.visitInsn(ICONST_4);
		mv.visitVarInsn(ILOAD, 6);
		mv.visitInsn(ISUB);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putBytes", "([BII)V", true);
		mv.visitLabel(l9);
		mv.visitFrame(Opcodes.F_CHOP,2, null, 0, null);
		mv.visitInsn(RETURN);
		Label l17 = new Label();
		mv.visitLabel(l17);
		mv.visitMaxs(6, 7);
		mv.visitEnd();
		}
		{
		mv = cw.visitMethod(ACC_PRIVATE, "putLongVal", "(Ltransfer/Outputable;Ljava/lang/Number;)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
		mv.visitVarInsn(LSTORE, 3);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 5);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitVarInsn(LLOAD, 3);
		mv.visitInsn(LCONST_0);
		mv.visitInsn(LCMP);
		Label l3 = new Label();
		mv.visitJumpInsn(IFGE, l3);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitVarInsn(LLOAD, 3);
		mv.visitInsn(LNEG);
		mv.visitVarInsn(LSTORE, 3);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitIntInsn(BIPUSH, 8);
		mv.visitVarInsn(ISTORE, 5);
		mv.visitLabel(l3);
		mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.LONG, Opcodes.INTEGER}, 0, null);
		mv.visitVarInsn(LLOAD, 3);
		mv.visitLdcInsn(new Long(128L));
		mv.visitInsn(LCMP);
		Label l6 = new Label();
		mv.visitJumpInsn(IFGE, l6);
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_2);
		mv.visitIntInsn(NEWARRAY, T_BYTE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_0);
		mv.visitIntInsn(BIPUSH, 16);
		mv.visitVarInsn(ILOAD, 5);
		mv.visitInsn(IOR);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IOR);
		mv.visitInsn(I2B);
		mv.visitInsn(BASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_1);
		mv.visitVarInsn(LLOAD, 3);
		mv.visitInsn(L2I);
		mv.visitInsn(I2B);
		mv.visitInsn(BASTORE);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "([B)V", true);
		Label l8 = new Label();
		mv.visitLabel(l8);
		Label l9 = new Label();
		mv.visitJumpInsn(GOTO, l9);
		mv.visitLabel(l6);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitIntInsn(BIPUSH, 8);
		mv.visitIntInsn(NEWARRAY, T_BYTE);
		mv.visitVarInsn(ASTORE, 6);
		Label l10 = new Label();
		mv.visitLabel(l10);
		mv.visitIntInsn(BIPUSH, 8);
		mv.visitVarInsn(ISTORE, 7);
		Label l11 = new Label();
		mv.visitLabel(l11);
		Label l12 = new Label();
		mv.visitJumpInsn(GOTO, l12);
		Label l13 = new Label();
		mv.visitLabel(l13);
		mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"[B", Opcodes.INTEGER}, 0, null);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitIincInsn(7, -1);
		mv.visitVarInsn(ILOAD, 7);
		mv.visitVarInsn(LLOAD, 3);
		mv.visitInsn(L2I);
		mv.visitInsn(I2B);
		mv.visitInsn(BASTORE);
		Label l14 = new Label();
		mv.visitLabel(l14);
		mv.visitVarInsn(LLOAD, 3);
		mv.visitIntInsn(BIPUSH, 8);
		mv.visitInsn(LSHR);
		mv.visitVarInsn(LSTORE, 3);
		mv.visitLabel(l12);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(LLOAD, 3);
		mv.visitInsn(LCONST_0);
		mv.visitInsn(LCMP);
		mv.visitJumpInsn(IFGT, l13);
		Label l15 = new Label();
		mv.visitLabel(l15);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitIntInsn(BIPUSH, 16);
		mv.visitVarInsn(ILOAD, 5);
		mv.visitInsn(IOR);
		mv.visitIntInsn(BIPUSH, 7);
		mv.visitVarInsn(ILOAD, 7);
		mv.visitInsn(ISUB);
		mv.visitInsn(IOR);
		mv.visitInsn(I2B);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);
		Label l16 = new Label();
		mv.visitLabel(l16);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitVarInsn(ILOAD, 7);
		mv.visitIntInsn(BIPUSH, 8);
		mv.visitVarInsn(ILOAD, 7);
		mv.visitInsn(ISUB);
		mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putBytes", "([BII)V", true);
		mv.visitLabel(l9);
		mv.visitFrame(Opcodes.F_CHOP,2, null, 0, null);
		mv.visitInsn(RETURN);
		Label l17 = new Label();
		mv.visitLabel(l17);
		mv.visitMaxs(6, 8);
		mv.visitEnd();
		}
	}


    private static NumberSerializer instance = new NumberSerializer();

    public static NumberSerializer getInstance() {
        return instance;
    }

}
