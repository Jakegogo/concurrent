package transfer.deserializer;

import utils.enhance.asm.util.AsmUtils;

import org.apache.mina.util.ConcurrentHashSet;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.ByteMeta;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exceptions.IllegalTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 集合解析器
 * <br/>尽量指定泛型类型,可提升解析性能
 * Created by Jake on 2015/2/23.
 */
public class CollectionDeSerializer implements Deserializer, Opcodes {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);
        if (typeFlag != Types.COLLECTION && typeFlag != Types.ARRAY) {
            throw new IllegalTypeException(typeFlag, Types.COLLECTION, type);
        }

        Collection list = createCollection(type);
        // 读取集合的大小
        int size = BitUtils.getInt(inputable);
        if (size == 0) {
            return (T) list;
        }


        Type itemType = null;
        if (type instanceof ParameterizedType) {
            itemType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
        } else if (type instanceof Class<?> && ((Class<?>)type).isArray()) {
            itemType = ((Class<?>)type).getComponentType();
        }


        Deserializer defaultComponentDeserializer = null;
        Class<?> componentClass = TypeUtils.getRawClass(itemType);
        if (itemType != null 
        		&& itemType != Object.class
        		&& !componentClass.isInterface()
				&& (componentClass.isArray() || !Modifier.isAbstract(componentClass.getModifiers()))) {
            defaultComponentDeserializer = TransferConfig.getDeserializer(itemType);// 元素解析器
        }


        // 循环解析元素
        Object component;
        if (defaultComponentDeserializer == null) {
            for (int i = 0; i < size;i++) {
                byte elementFlag = inputable.getByte();
                Deserializer componentDeserializer = TransferConfig.getDeserializer(itemType, elementFlag);// 元素解析器
                component =  componentDeserializer.deserialze(inputable, itemType, elementFlag, referenceMap);
                list.add(component);
            }
        } else {
            for (int i = 0; i < size;i++) {
                component = defaultComponentDeserializer.deserialze(inputable, itemType, inputable.getByte(), referenceMap);
                list.add(component);
            }
        }

        return (T) list;
    }


    protected Collection createCollection(Type type) {

        if (type == null || type == Collection.class || type == Object.class) {
            return new ArrayList();
        }

        Class<?> rawClass = TypeUtils.getRawClass(type);

        Collection list;

        if (rawClass == AbstractCollection.class) {
            list = new ArrayList();
        } else if (rawClass == ConcurrentHashSet.class) {
            list = new ConcurrentHashSet();
        } else if (rawClass.isAssignableFrom(HashSet.class)) {
            list = new HashSet();
        } else if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet();
        } else if (rawClass.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet();
        } else if (rawClass.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList();
        } else if (rawClass.isAssignableFrom(EnumSet.class)) {

            Type itemType;
            if (type instanceof ParameterizedType) {
                itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
            list = EnumSet.noneOf((Class<Enum>)itemType);

        } else {

            try {
                list = (Collection) rawClass.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("create instane error, class " + rawClass.getName());
            }

        }
        return list;
    }


    public ByteMeta readMeta(Inputable inputable) {
        byte flag = inputable.getByte();
        byte type = TransferConfig.getType(flag);

        if (type != Types.COLLECTION && type != Types.ARRAY) {
            throw new IllegalTypeException(type, Types.COLLECTION, null);
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
        mv.visitIntInsn(BIPUSH, Types.COLLECTION);
        Label l2 = new Label();
        mv.visitJumpInsn(IF_ICMPEQ, l2);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitIntInsn(BIPUSH, Types.ARRAY);
        mv.visitJumpInsn(IF_ICMPEQ, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitIntInsn(BIPUSH, Types.COLLECTION);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(BBLjava/lang/reflect/Type;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l2);


        Class<?> rawClass = TypeUtils.getRawClass(type);
        if (rawClass == null || rawClass == Collection.class || rawClass == Object.class) {

            // new ArrayList()
            mv.visitTypeInsn(NEW, AsmUtils.toAsmCls(ArrayList.class.getName()));
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, AsmUtils.toAsmCls(ArrayList.class.getName()), "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 6);

        } else {
            compileCreateCollection(type, mv);
            mv.visitVarInsn(ASTORE, 6);
        }


        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "getInt", "(Ltransfer/Inputable;)I", false);
        mv.visitVarInsn(ISTORE, 7);

        mv.visitVarInsn(ILOAD, 7);
        Label l6 = new Label();
        mv.visitJumpInsn(IFNE, l6);
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitVarInsn(ALOAD, 6);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l6);


        Type itemType = Object.class;
        if (type instanceof ParameterizedType) {
            itemType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
        } else if (type instanceof Class<?> && ((Class<?>)type).isArray()) {
            itemType = ((Class<?>)type).getComponentType();
        }


        Class<?> componentClass = TypeUtils.getRawClass(itemType);
        Deserializer defaultComponentDeserializer = null;
        if (itemType != null && itemType != Object.class
        		&& !componentClass.isInterface()
				&& (componentClass.isArray() || !Modifier.isAbstract(componentClass.getModifiers()))) {
            defaultComponentDeserializer = TransferConfig.getDeserializer(itemType);// 元素解析器
        }


        if (defaultComponentDeserializer == null) {

            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 8);

            Label l18 = new Label();
            mv.visitLabel(l18);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Opcodes.TOP, Opcodes.INTEGER}, 0, null);
            mv.visitVarInsn(ILOAD, 8);
            mv.visitVarInsn(ILOAD, 7);

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

            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(POP);

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
            mv.visitVarInsn(ILOAD, 7);

            Label l25 = new Label();
            mv.visitJumpInsn(IF_ICMPGE, l25);


            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(TypeUtils.getRawClass(itemType).getName()) + ";"));
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
            mv.visitVarInsn(ALOAD, 4);

            MethodVisitor methodVisitor = context.invokeNextDeserialize(null, mv);
            defaultComponentDeserializer.compile(itemType, methodVisitor, context);

            mv.visitVarInsn(ASTORE, 10);

            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ALOAD, 10);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(POP);

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

    // 预编译创建集合
    private void compileCreateCollection(Type type, MethodVisitor mv) {

        Class<?> rawClass = TypeUtils.getRawClass(type);

        if (type == AbstractCollection.class) {
            rawClass = ArrayList.class;
        } else if (type == ConcurrentHashSet.class) {
            rawClass = ConcurrentHashSet.class;
        } else if (rawClass.isAssignableFrom(HashSet.class)) {
            rawClass = HashSet.class;
        } else if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
            rawClass = LinkedHashSet.class;
        } else if (rawClass.isAssignableFrom(TreeSet.class)) {
            rawClass = TreeSet.class;
        } else if (rawClass.isAssignableFrom(ArrayList.class)) {
            rawClass = ArrayList.class;
        } else if (rawClass.isAssignableFrom(EnumSet.class)) {// 枚举Set

            Type itemType;
            if (type instanceof ParameterizedType) {
                itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }

            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(TypeUtils.getRawClass(itemType).getName()) + ";"));
            mv.visitMethodInsn(INVOKESTATIC, "java/util/EnumSet", "noneOf", "(Ljava/lang/Class;)Ljava/util/EnumSet;", false);
            mv.visitVarInsn(ASTORE, 6);
            return;
        }

        mv.visitTypeInsn(NEW, AsmUtils.toAsmCls(rawClass.getName()));
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, AsmUtils.toAsmCls(rawClass.getName()), "<init>", "()V", false);

    }


    private static CollectionDeSerializer instance = new CollectionDeSerializer();

    public static CollectionDeSerializer getInstance() {
        return instance;
    }

}
