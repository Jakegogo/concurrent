package transfer.deserializer;

import dbcache.support.asm.util.AsmUtils;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.ByteMeta;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exception.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 数组解析器
 * <br/>尽量指定元素类型,可提升解析性能
 * Created by Jake on 2015/2/23.
 */
public class ArrayDeSerializer implements Deserializer, Opcodes {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.ARRAY && typeFlag != Types.COLLECTION) {
            throw new IllegalTypeException(typeFlag, Types.ARRAY, type);
        }

        // 读取数组的大小
        int size = BitUtils.getInt(inputable);

        Type itemType = null;
        if (type instanceof Class<?> && ((Class<?>)type).isArray()) {
            itemType = ((Class<?>)type).getComponentType();
        } else if (type instanceof ParameterizedType) {
            itemType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
        }


        Object array = Array.newInstance(TypeUtils.getRawClass(itemType), size);
        if (size == 0) {
            return (T) array;
        }


        Class<?> componentClass = TypeUtils.getRawClass(itemType);
        Deserializer defaultComponentDeserializer = null;
        if (itemType != null 
        		&& itemType != Object.class
        		&& !componentClass.isInterface()
				&& !Modifier.isAbstract(componentClass.getModifiers())) {
            defaultComponentDeserializer = TransferConfig.getDeserializer(itemType);// 元素解析器
        }


        // 循环解析元素
        Object component;
        if (defaultComponentDeserializer == null) {
            for (int i = 0; i < size;i++) {
                byte elementFlag = inputable.getByte();
                Deserializer componentDeserializer = TransferConfig.getDeserializer(itemType, elementFlag);// 元素解析器
                component =  componentDeserializer.deserialze(inputable, itemType, elementFlag, referenceMap);
                Array.set(array, i, component);
            }
        } else {
            for (int i = 0; i < size;i++) {
                component = defaultComponentDeserializer.deserialze(inputable, itemType, inputable.getByte(), referenceMap);
                Array.set(array, i, component);
            }
        }

        return (T) array;
    }


    public ByteMeta readMeta(Inputable inputable) {

        byte flag = inputable.getByte();
        byte type = TransferConfig.getType(flag);

        if (type != Types.ARRAY && type != Types.COLLECTION) {
            throw new IllegalTypeException(type, Types.ARRAY, null);
        }
        // 读取集合的大小
        int size = BitUtils.getInt(inputable);

        ByteMeta byteDataMeta = new ByteMeta();
        byteDataMeta.setComponentSize(size);
        byteDataMeta.setFlag(flag);
        byteDataMeta.setIteratorAble(true);

        return byteDataMeta;
    }

    
    @Override
	public void compile(Type type, MethodVisitor mv,
			AsmDeserializerContext context) {

        mv.visitCode();
        mv.visitVarInsn(ILOAD, 3);
        mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getType", "(B)B", false);
        mv.visitVarInsn(ISTORE, 5);

        mv.visitVarInsn(ILOAD, 5);
        mv.visitIntInsn(BIPUSH, Types.COLLECTION);
        Label l2 = new Label();
        mv.visitJumpInsn(IF_ICMPEQ, l2);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitIntInsn(BIPUSH, Types.ARRAY);
        mv.visitJumpInsn(IF_ICMPEQ, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitTypeInsn(NEW, "transfer/exception/IllegalTypeException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitIntInsn(BIPUSH, Types.COLLECTION);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, "transfer/exception/IllegalTypeException", "<init>", "(BBLjava/lang/reflect/Type;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l2);


        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
        mv.visitVarInsn(ISTORE, 6);

        mv.visitVarInsn(ILOAD, 6);
        Label l6 = new Label();
        mv.visitJumpInsn(IFNE, l6);
        mv.visitInsn(ICONST_0);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        mv.visitInsn(ARETURN);
        mv.visitLabel(l6);


        Type itemType = Object.class;
        if (type instanceof ParameterizedType) {
            itemType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
        } else if (type instanceof Class<?> && ((Class<?>)type).isArray()) {
            itemType = ((Class<?>)type).getComponentType();
        }


        if (itemType == null) {
            itemType = Object.class;
        }

        // 创建数组
        mv.visitVarInsn(ILOAD, 6);
        mv.visitTypeInsn(ANEWARRAY, AsmUtils.toAsmCls(TypeUtils.getRawClass(itemType).getName()));
        mv.visitVarInsn(ASTORE, 7);
        
        
        Class<?> componentClass = TypeUtils.getRawClass(itemType);
        Deserializer defaultComponentDeserializer = null;
        if (itemType != null && itemType != Object.class
        		&& !componentClass.isInterface()
				&& !Modifier.isAbstract(componentClass.getModifiers())) {
            defaultComponentDeserializer = TransferConfig.getDeserializer(itemType);// 元素解析器
        }


        if (defaultComponentDeserializer == null) {

            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 8);

            Label l18 = new Label();
            mv.visitLabel(l18);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Opcodes.TOP, Opcodes.INTEGER}, 0, null);
            mv.visitVarInsn(ILOAD, 8);
            mv.visitVarInsn(ILOAD, 6);

            Label l19 = new Label();
            mv.visitJumpInsn(IF_ICMPGE, l19);

            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
            mv.visitVarInsn(ISTORE, 9);

            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(TypeUtils.getRawClass(itemType).getName()) + ";"));
            mv.visitVarInsn(ILOAD, 9);
            mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getDeserializer", "(Ljava/lang/reflect/Type;B)Ltransfer/deserializer/Deserializer;", false);
            mv.visitVarInsn(ASTORE, 10);

            mv.visitVarInsn(ALOAD, 10);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(TypeUtils.getRawClass(itemType).getName()) + ";"));
            mv.visitVarInsn(ILOAD, 9);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "transfer/deserializer/Deserializer", "deserialze", "(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/utils/IntegerMap;)Ljava/lang/Object;", true);
            mv.visitVarInsn(ASTORE, 11);

            //array[i] = obj;
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ILOAD, 8);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitInsn(AsmUtils.storeArrayCode(org.objectweb.asm.Type.getType(componentClass)));

            mv.visitIincInsn(8, 1);
            mv.visitJumpInsn(GOTO, l18);

            mv.visitLabel(l19);

        } else {


            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 8);

            Label l26 = new Label();
            mv.visitLabel(l26);

            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Opcodes.TOP, Opcodes.INTEGER}, 0, null);
            mv.visitVarInsn(ILOAD, 8);
            mv.visitVarInsn(ILOAD, 6);

            Label l25 = new Label();
            mv.visitJumpInsn(IF_ICMPGE, l25);


            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(TypeUtils.getRawClass(itemType).getName()) + ";"));
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
            mv.visitVarInsn(ALOAD, 4);

            context.invokeNextDeserialize(null, mv);
            defaultComponentDeserializer.compile(itemType, mv, context);

            mv.visitVarInsn(ASTORE, 10);

            //array[i] = obj;
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ILOAD, 8);
            mv.visitVarInsn(ALOAD, 10);
            mv.visitInsn(AsmUtils.storeArrayCode(org.objectweb.asm.Type.getType(componentClass)));

            mv.visitIincInsn(8, 1);
            mv.visitJumpInsn(GOTO, l26);
            mv.visitLabel(l25);
        }

        mv.visitFrame(Opcodes.F_CHOP,2, null, 0, null);
        mv.visitVarInsn(ALOAD, 6);
        mv.visitInsn(ARETURN);

        mv.visitMaxs(5, 14);
        mv.visitEnd();

	}


    private static ArrayDeSerializer instance = new ArrayDeSerializer();

    public static ArrayDeSerializer getInstance() {
        return instance;
    }

}
