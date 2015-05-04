package transfer.deserializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.EnumInfo;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exceptions.IllegalClassTypeException;
import transfer.exceptions.IllegalTypeException;
import transfer.exceptions.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;
import utils.enhance.asm.util.AsmUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * 枚举解析器
 * Created by Jake on 2015/2/25.
 */
public class EnumDeserializer implements Deserializer, Opcodes {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.ENUM) {
            throw new IllegalTypeException(typeFlag, Types.ENUM, type);
        }

        // 读取枚举类型
        int enumType = BitUtils.getInt(inputable);

        Class<?> rawClass = TypeUtils.getRawClass(type);

        if (type == null 
        		|| type == Object.class 
        		|| type == Enum.class
        		|| rawClass.isInterface()
				|| Modifier.isAbstract(rawClass.getModifiers()) && !rawClass.isArray()) {

            rawClass = TransferConfig.getClass(enumType);

        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(type);
        }

        EnumInfo enumInfo = (EnumInfo) TransferConfig.getOrCreateClassInfo(rawClass);

        if (enumInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        if (enumType != enumInfo.getClassId()) {
            throw new IllegalClassTypeException(enumType, type);
        }

        // 读取枚举索引
        int enumIndex = BitUtils.getInt(inputable);

        return (T) enumInfo.toEnum(enumIndex);
    }

    
    @Override
	public void compile(Type type, MethodVisitor mv,
			AsmDeserializerContext context) {
    	
    	mv.visitCode();
    	mv.visitVarInsn(ILOAD, 3);
    	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getType", "(B)B", false);
    	mv.visitVarInsn(ISTORE, 5);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.ENUM);
    	
    	Label l2 = new Label();
    	mv.visitJumpInsn(IF_ICMPEQ, l2);
    	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
    	mv.visitInsn(DUP);
    	mv.visitVarInsn(ILOAD, 5);
    	mv.visitIntInsn(BIPUSH, Types.ENUM);
    	mv.visitVarInsn(ALOAD, 2);
    	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(BBLjava/lang/reflect/Type;)V", false);
    	mv.visitInsn(ATHROW);
    	mv.visitLabel(l2);
        
        
    	Class<?> rawClass = TypeUtils.getRawClass(type);

        if (type == null 
        		|| type == Object.class 
        		|| type == Enum.class
        		|| rawClass.isInterface()
				|| Modifier.isAbstract(rawClass.getModifiers()) && !rawClass.isArray()) {

        	mv.visitVarInsn(ALOAD, 1);
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
        	mv.visitVarInsn(ISTORE, 6);
        	
        	mv.visitVarInsn(ILOAD, 6);
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getClass", "(I)Ljava/lang/Class;", false);
        	mv.visitVarInsn(ASTORE, 7);
        	
        	mv.visitVarInsn(ALOAD, 7);
        	Label l12 = new Label();
        	mv.visitJumpInsn(IFNONNULL, l12);
        	mv.visitTypeInsn(NEW, "transfer/exceptions/UnsupportDeserializerTypeException");
        	mv.visitInsn(DUP);
        	mv.visitVarInsn(ALOAD, 2);
        	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/UnsupportDeserializerTypeException", "<init>", "(Ljava/lang/reflect/Type;)V", false);
        	mv.visitInsn(ATHROW);
        	mv.visitLabel(l12);
        	
        	
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getOrCreateClassInfo", "(Ljava/lang/Class;)Ltransfer/core/ClassInfo;", false);
        	mv.visitTypeInsn(CHECKCAST, "transfer/core/EnumInfo");
        	mv.visitVarInsn(ASTORE, 8);
        	mv.visitVarInsn(ALOAD, 8);
        	Label l15 = new Label();
        	mv.visitJumpInsn(IFNONNULL, l15);
        	mv.visitTypeInsn(NEW, "transfer/exceptions/UnsupportDeserializerTypeException");
        	mv.visitInsn(DUP);
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/UnsupportDeserializerTypeException", "<init>", "(Ljava/lang/reflect/Type;)V", false);
        	mv.visitInsn(ATHROW);
        	mv.visitLabel(l15);
        	
        	mv.visitVarInsn(ILOAD, 6);
        	mv.visitVarInsn(ALOAD, 8);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "transfer/core/EnumInfo", "getClassId", "()I", false);
        	Label l17 = new Label();
        	mv.visitJumpInsn(IF_ICMPEQ, l17);
        	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalClassTypeException");
        	mv.visitInsn(DUP);
        	mv.visitVarInsn(ILOAD, 6);
        	mv.visitVarInsn(ALOAD, 2);
        	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalClassTypeException", "<init>", "(ILjava/lang/reflect/Type;)V", false);
        	mv.visitInsn(ATHROW);
        	mv.visitLabel(l17);
        	
        	mv.visitVarInsn(ALOAD, 1);
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
        	mv.visitVarInsn(ISTORE, 9);
        	mv.visitVarInsn(ALOAD, 8);
        	mv.visitVarInsn(ILOAD, 9);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "transfer/core/EnumInfo", "toEnum", "(I)Ljava/lang/Enum;", false);
        	mv.visitInsn(ARETURN);
        	mv.visitMaxs(5, 10);
        	mv.visitEnd();
        	
        } else {
        	
        	mv.visitVarInsn(ALOAD, 1);
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
        	mv.visitVarInsn(ISTORE, 6);
        	
        	mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(rawClass.getName()) + ";"));
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getOrCreateClassInfo", "(Ljava/lang/Class;)Ltransfer/core/ClassInfo;", false);
        	mv.visitTypeInsn(CHECKCAST, "transfer/core/EnumInfo");
        	mv.visitVarInsn(ASTORE, 7);
        	mv.visitVarInsn(ALOAD, 7);
        	
        	Label l15 = new Label();
        	mv.visitJumpInsn(IFNONNULL, l15);
        	mv.visitTypeInsn(NEW, "transfer/exceptions/UnsupportDeserializerTypeException");
        	mv.visitInsn(DUP);
        	mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(rawClass.getName()) + ";"));
        	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/UnsupportDeserializerTypeException", "<init>", "(Ljava/lang/reflect/Type;)V", false);
        	mv.visitInsn(ATHROW);
        	mv.visitLabel(l15);
        	
        	mv.visitVarInsn(ILOAD, 6);
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "transfer/core/EnumInfo", "getClassId", "()I", false);
        	Label l17 = new Label();
        	mv.visitJumpInsn(IF_ICMPEQ, l17);
        	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalClassTypeException");
        	mv.visitInsn(DUP);
        	mv.visitVarInsn(ILOAD, 6);
        	mv.visitVarInsn(ALOAD, 2);
        	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalClassTypeException", "<init>", "(ILjava/lang/reflect/Type;)V", false);
        	mv.visitInsn(ATHROW);
        	mv.visitLabel(l17);
        	
        	mv.visitVarInsn(ALOAD, 1);
        	mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
        	mv.visitVarInsn(ISTORE, 8);
        	
        	mv.visitVarInsn(ALOAD, 7);
        	mv.visitVarInsn(ILOAD, 8);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "transfer/core/EnumInfo", "toEnum", "(I)Ljava/lang/Enum;", false);
        	mv.visitInsn(ARETURN);
        	mv.visitMaxs(5, 10);
        	mv.visitEnd();
        	
        }
    	
    	
	}
    

    private static EnumDeserializer instance = new EnumDeserializer();

    public static EnumDeserializer getInstance() {
        return instance;
    }

}
