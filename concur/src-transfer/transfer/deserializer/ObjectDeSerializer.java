package transfer.deserializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.ClassInfo;
import transfer.core.DeserialContext;
import transfer.core.FieldInfo;
import transfer.core.ParseStackTrace;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exceptions.CompileError;
import transfer.exceptions.IllegalClassTypeException;
import transfer.exceptions.IllegalTypeException;
import transfer.exceptions.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.TypeUtils;
import utils.enhance.asm.util.AsmUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * 对象解析器
 * Created by Jake on 2015/2/23.
 */
public class ObjectDeSerializer implements Deserializer, Opcodes {



    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, DeserialContext context) {

        ParseStackTrace stack = context.nextStackTrace(type);

        byte typeFlag = TransferConfig.getType(flag);
        if (typeFlag != Types.OBJECT) {
            throw new IllegalTypeException(context, typeFlag, Types.OBJECT, type);
        }

        // 读取对象类型
        int classId = BitUtils.getInt(inputable);
        Class<?> rawClass = TypeUtils.getRawClass(type);
        if (type == null 
        		|| type == Object.class
        		|| rawClass.isInterface()
				|| Modifier.isAbstract(rawClass.getModifiers()) && !rawClass.isArray()) {
            rawClass = TransferConfig.getClass(classId);
        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(type);
        }

        ClassInfo classInfo = TransferConfig.getOrCreateClassInfo(rawClass);

        if (classInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        if (classId != classInfo.getClassId()) {
            throw new IllegalClassTypeException(context, classId, type);
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

            context.next(stack, "field [" + fieldInfo.getName() + "]");

            byte fieldFlag = inputable.getByte();
            fieldType = fieldInfo.getType();
            fieldDeserializer = TransferConfig.getDeserializer(fieldType, fieldFlag);

            fieldValue = fieldDeserializer.deserialze(inputable, fieldType, fieldFlag, context);
            fieldInfo.setField(object, fieldValue);
        }

        return (T) object;
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
        mv.visitIntInsn(BIPUSH, Types.OBJECT);
        
        Label l5 = new Label();
        mv.visitJumpInsn(IF_ICMPEQ, l5);
        mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitIntInsn(BIPUSH, Types.OBJECT);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(Ltransfer/core/DeserialContext;BBLjava/lang/reflect/Type;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l5);

        mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
        mv.visitVarInsn(ISTORE, 6);


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

        // 判断类型
        if (rawClass == Object.class) {
            throw new CompileError("不支持编译类型:" + Object.class);
        }

        mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"transfer/core/ClassInfo"}, 0, null);
        mv.visitVarInsn(ILOAD, 6);
        mv.visitIntInsn(SIPUSH, classInfo.getClassId());

        Label l14 = new Label();
        mv.visitJumpInsn(IF_ICMPEQ, l14);
        mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalClassTypeException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitVarInsn(ILOAD, 6);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalClassTypeException", "<init>", "(Ltransfer/core/DeserialContext;ILjava/lang/reflect/Type;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l14);
        
        // new Entity()
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

        mv.visitTypeInsn(NEW, AsmUtils.toAsmCls(rawClass.getName()));
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, AsmUtils.toAsmCls(rawClass.getName()), "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 7);


        int localIndex = 7;
        Type fieldType;
        Deserializer fieldDeserializer;
        for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
            fieldType = fieldInfo.getType();
            
            Class<?> fieldRawClass = TypeUtils.getRawClass(fieldType);
            if (fieldType == null || fieldType == Object.class
					|| fieldRawClass.isInterface()
					|| Modifier.isAbstract(fieldRawClass.getModifiers()) 
					&& !fieldRawClass.isArray() 
					&& !fieldRawClass.isPrimitive()) {// 使用默认解析器
            	
            	mv.visitVarInsn(ALOAD, 1);
            	mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
            	int store1 = ++localIndex;
            	mv.visitVarInsn(ISTORE, store1);
            	
            	
            	mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(Object.class.getName()) + ";"));
            	mv.visitVarInsn(ILOAD, store1);
            	mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getDeserializer", "(Ljava/lang/reflect/Type;B)Ltransfer/deserializer/Deserializer;", false);
            	
            	mv.visitVarInsn(ALOAD, 1);
            	mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(Object.class.getName()) + ";"));
            	mv.visitVarInsn(ILOAD, store1);
            	mv.visitVarInsn(ALOAD, 4);
            	mv.visitMethodInsn(INVOKEINTERFACE, "transfer/deserializer/Deserializer", "deserialze", "(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/core/DeserialContext;)Ljava/lang/Object;", true);
            	mv.visitVarInsn(ASTORE, ++localIndex);
            	
            } else {
            	
            	// 使用预编译解析方法
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
                int store1 = ++localIndex;
                mv.visitVarInsn(ISTORE, store1);
                
                
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                if (fieldRawClass.isPrimitive()) {
                	AsmUtils.loadPrimitiveType(mv, fieldRawClass);
                } else {
                	mv.visitLdcInsn(org.objectweb.asm.Type.getType(fieldRawClass));
                }
                mv.visitVarInsn(ILOAD, store1);
                mv.visitVarInsn(ALOAD, 4);
                
                fieldDeserializer = TransferConfig.getDeserializer(fieldType, Types.UNKOWN);
                // 执行属性预编译
                MethodVisitor mv1 = context.invokeNextDeserialize(fieldInfo.getFieldName(), mv);
                fieldDeserializer.compile(fieldType, mv1, context);
                
                mv.visitVarInsn(ASTORE, ++localIndex);
                
            }
            
            // 设置属性
            PropertyDescriptor propertyDescriptor;
			try {
				propertyDescriptor = new PropertyDescriptor(fieldInfo.getFieldName(), rawClass);
			} catch (IntrospectionException e) {
				e.printStackTrace();
				throw new CompileError(e);
			}
			
			//获取set方法
			final Method setMethod = propertyDescriptor.getWriteMethod();
			final org.objectweb.asm.Type[] mat = org.objectweb.asm.Type.getArgumentTypes(setMethod);
			final Class<?>[] mpt = setMethod.getParameterTypes();
			final org.objectweb.asm.Type mrt = org.objectweb.asm.Type.getType(setMethod);
			
			//获取this.target
			mv.visitVarInsn(ALOAD, 7);
			mv.visitVarInsn(ALOAD, localIndex);

			if (mpt[0].isPrimitive()) {
				// unBoxing
				AsmUtils.withUnBoxingType(mv, mat[0]);
			} else if(mpt[0] != Object.class) {
				mv.visitTypeInsn(CHECKCAST, AsmUtils.toAsmCls(mpt[0].getName()));
			}

			mv.visitMethodInsn(INVOKEVIRTUAL,
					AsmUtils.toAsmCls(rawClass.getName()), setMethod.getName(),
					mrt.toString(), false);

        }

        mv.visitFrame(Opcodes.F_FULL, 10, new Object[] {"transfer/deserializer/ObjectDeSerializer", "transfer/Inputable", "java/lang/reflect/Type", Opcodes.INTEGER, "transfer/utils/IntegerMap", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/Class", "transfer/core/ClassInfo", "java/lang/Object"}, 0, new Object[] {});
        mv.visitVarInsn(ALOAD, 7);
        mv.visitInsn(ARETURN);

        mv.visitMaxs(5, 16);
        mv.visitEnd();
        
	}
    

    private static final ObjectDeSerializer instance = new ObjectDeSerializer();

    public static ObjectDeSerializer getInstance() {
        return instance;
    }

}
