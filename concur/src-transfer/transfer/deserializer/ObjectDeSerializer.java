package transfer.deserializer;

import dbcache.support.asm.util.AsmUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.ClassInfo;
import transfer.core.FieldInfo;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.CompileError;
import transfer.exception.IllegalClassTypeException;
import transfer.exception.IllegalTypeException;
import transfer.exception.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;

/**
 * 对象解析器
 * Created by Jake on 2015/2/23.
 */
public class ObjectDeSerializer implements Deserializer, Opcodes {



    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.OBJECT) {
            throw new IllegalTypeException(typeFlag, Types.OBJECT, type);
        }

        // 读取对象类型
        int classId = BitUtils.getInt2(inputable);

        Class<?> rawClass;

        if (type == null || type == Object.class) {

            rawClass = TransferConfig.getClass(classId);
        } else {

            rawClass = TypeUtils.getRawClass(type);
        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        ClassInfo classInfo = TransferConfig.getOrCreateClassInfo(rawClass);

        if (classInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        if (classId != classInfo.getClassId()) {
            throw new IllegalClassTypeException(classId, type);
        }

        Object object;
        try {
            object = rawClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("create instane error, class " + rawClass.getName());
        }

        Type fieldType;
        Object fieldValue;
        Deserializer fieldDeserializer;

        for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {

            byte fieldFlag = inputable.getByte();

            fieldType = fieldInfo.getType();

            fieldDeserializer = TransferConfig.getDeserializer(fieldType, fieldFlag);

            fieldValue = fieldDeserializer.deserialze(inputable, fieldType, fieldFlag, referenceMap);

            fieldInfo.setField(object, fieldValue);

        }

        return (T) object;
    }
    

    
    @Override
	public void compile(Type type, MethodVisitor mv,
			AsmDeserializerContext context) {
        mv.visitCode();

        mv.visitVarInsn(ILOAD, 3);
        mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getType", "(B)B", false);
        mv.visitVarInsn(ISTORE, 5);

        mv.visitVarInsn(ILOAD, 5);
        mv.visitIntInsn(BIPUSH, Types.OBJECT);
        Label l5 = new Label();
        mv.visitJumpInsn(IF_ICMPEQ, l5);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitTypeInsn(NEW, "transfer/exception/IllegalTypeException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitIntInsn(BIPUSH, Types.OBJECT);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, "transfer/exception/IllegalTypeException", "<init>", "(BBLjava/lang/reflect/Type;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l5);

        mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt2", "(Ltransfer/Inputable;)I", false);
        mv.visitVarInsn(ISTORE, 6);

        mv.visitVarInsn(ALOAD, 2);
        Label l8 = new Label();
        mv.visitJumpInsn(IFNULL, l8);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitLdcInsn(org.objectweb.asm.Type.getType("Ljava/lang/Object;"));
        Label l9 = new Label();
        mv.visitJumpInsn(IF_ACMPNE, l9);
        mv.visitLabel(l8);


        Class<?> rawClass;

        if (type == null || type == Object.class) {

            throw new CompileError("不支持编译类型:" + type);
        } else {

            rawClass = TypeUtils.getRawClass(type);
        }

        if (rawClass == null) {
            throw new CompileError("不支持编译类型:" + type);
        }

        ClassInfo classInfo = TransferConfig.getOrCreateClassInfo(rawClass);

        if (classInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }


        mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"transfer/core/ClassInfo"}, 0, null);
        mv.visitVarInsn(ILOAD, 6);
        mv.visitIntInsn(SIPUSH, classInfo.getClassId());

        Label l14 = new Label();
        mv.visitJumpInsn(IF_ICMPEQ, l14);
        mv.visitTypeInsn(NEW, "transfer/exception/IllegalClassTypeException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 6);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, "transfer/exception/IllegalClassTypeException", "<init>", "(ILjava/lang/reflect/Type;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l14);

        //} catch (Exception e) {
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

        mv.visitTypeInsn(NEW, AsmUtils.toAsmCls(rawClass.getName()));
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, AsmUtils.toAsmCls(rawClass.getName()), "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 9);


        int localIndex = 10;
        Type fieldType;
        Deserializer fieldDeserializer;
        for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {

            fieldType = fieldInfo.getType();
            if (fieldType == null || fieldType == Object.class) {



            } else {

                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
                int store1 = localIndex++;
                mv.visitVarInsn(ISTORE, store1);


                fieldDeserializer = TransferConfig.getDeserializer(fieldType, Types.UNKOWN);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(TypeUtils.getRawClass(fieldType).getName()) + ";"));
                mv.visitVarInsn(ILOAD, store1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "transfer/deserializer/Deserializer", "deserialze", "(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/utils/IntegerMap;)Ljava/lang/Object;", true);
                int store2 = localIndex++;
                mv.visitVarInsn(ASTORE, store2);

                // 执行属性预编译
                MethodVisitor mv1 = context.invokeNextDeserialize(fieldInfo.getFieldName(), mv);
                fieldDeserializer.compile(fieldType, mv1, context);

//TODO
                mv.visitVarInsn(ALOAD, 9);
                mv.visitVarInsn(ALOAD, store2);
                mv.visitMethodInsn(INVOKEVIRTUAL, "transfer/core/FieldInfo", "setField", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
            }

        }

        mv.visitFrame(Opcodes.F_FULL, 10, new Object[] {"transfer/deserializer/ObjectDeSerializer", "transfer/Inputable", "java/lang/reflect/Type", Opcodes.INTEGER, "transfer/utils/IntegerMap", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/Class", "transfer/core/ClassInfo", "java/lang/Object"}, 0, new Object[] {});
        mv.visitVarInsn(ALOAD, 9);
        mv.visitInsn(ARETURN);

        mv.visitMaxs(5, 16);
        mv.visitEnd();
	}
    

    private static ObjectDeSerializer instance = new ObjectDeSerializer();

    public static ObjectDeSerializer getInstance() {
        return instance;
    }

}
