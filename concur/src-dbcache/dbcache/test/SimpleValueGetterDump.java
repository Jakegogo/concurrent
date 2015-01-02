package dbcache.test;

import org.objectweb.asm.*;
public class SimpleValueGetterDump implements Opcodes {

    public static byte[] dump () throws Exception {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "SimpleValueGetter", "Ljava/lang/Object;Ldbcache/test/ValueGetter<Ldbcache/test/Entity;>;", "java/lang/Object", new String[] { "dbcache/test/ValueGetter" });

        cw.visitSource("SimpleValueGetter.java", null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(7, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "LSimpleValueGetter;", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "set", "(Ldbcache/test/Entity;Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(12, l0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
            mv.visitMethodInsn(INVOKEVIRTUAL, "dbcache/test/Entity", "setNum", "(I)V");
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(16, l1);
            mv.visitInsn(RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "LSimpleValueGetter;", null, l0, l2, 0);
            mv.visitLocalVariable("entity", "Ldbcache/test/Entity;", null, l0, l2, 1);
            mv.visitLocalVariable("object", "Ljava/lang/Object;", null, l0, l2, 2);
            mv.visitMaxs(2, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(1, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, "dbcache/test/Entity");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "SimpleValueGetter", "set", "(Ldbcache/test/Entity;Ljava/lang/Object;)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
