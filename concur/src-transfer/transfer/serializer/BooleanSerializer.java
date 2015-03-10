package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Outputable;
import transfer.compile.AsmContext;
import transfer.def.Types;
import transfer.utils.IdentityHashMap;

import java.lang.reflect.Type;

/**
 * 布尔编码器
 * Created by Jake on 2015/2/26.
 */
public class BooleanSerializer implements Serializer, Opcodes {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        Boolean bool = (Boolean) object;

        byte booleanVal;
        if (bool.booleanValue()) {
            booleanVal = (byte) 0x01;
        } else {
            booleanVal = (byte) 0x00;
        }

        outputable.putByte((byte) (Types.BOOLEAN | booleanVal));
    }

    @Override
    public void compile(Type type, MethodVisitor mv, AsmContext context) {
    	
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

        mv.visitVarInsn(ALOAD, 1);
        mv.visitIntInsn(BIPUSH, (int) Types.BOOLEAN);
        mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);

        
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitVarInsn(ASTORE, 4);
        
        
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        Label l5 = new Label();
        mv.visitJumpInsn(IFEQ, l5);

        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, 5);

        Label l8 = new Label();
        mv.visitJumpInsn(GOTO, l8);
        mv.visitLabel(l5);
        mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/lang/Boolean"}, 0, null);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 5);
        mv.visitLabel(l8);
        mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitIntInsn(BIPUSH, 48);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitInsn(IOR);
        mv.visitInsn(I2B);
        mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);

        mv.visitInsn(RETURN);

        mv.visitMaxs(4, 6);
        mv.visitEnd();
    	
    }


    private static BooleanSerializer instance = new BooleanSerializer();

    public static BooleanSerializer getInstance() {
        return instance;
    }

}
