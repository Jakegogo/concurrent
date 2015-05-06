package transfer.deserializer;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.ByteMeta;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.exceptions.IllegalTypeException;
import transfer.exceptions.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;
import utils.enhance.asm.util.AsmUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.util.ConcurrentHashSet;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Map解析器
 * <br/>尽量指定泛型类型,可提升解析性能
 * Created by Jake on 2015/2/24.
 */
public class MapDeSerializer implements Deserializer, Opcodes {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = TransferConfig.getType(flag);

        if (typeFlag != Types.MAP) {
            throw new IllegalTypeException(typeFlag, Types.MAP, type);
        }

        Map<Object, Object> map = createMap(type);

        // 读取数组的大小
        int size = BitUtils.getInt(inputable);

        if (size == 0) {
            return (T) map;
        }

        // 读取元素类型
        byte keyFlag;byte valueFlag;
        Object key;Object value;

        Type keyType = null;
        Type valueType = null;


        if (type instanceof ParameterizedType) {

            keyType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);

            valueType = TypeUtils.getParameterizedType((ParameterizedType) type, 1);
        }


        // 循环解析元素
        for (int i = 0; i < size;i++) {

            keyFlag = inputable.getByte();// 获取key类型

            key = parseElement(inputable, keyType, keyFlag, referenceMap);

            valueFlag = inputable.getByte();// 获取value类型
            
            value = parseElement(inputable, valueType, valueFlag, referenceMap);

            map.put(key, value);
        }


        return (T) map;
    }

    
    @Override
	public void compile(Type type, MethodVisitor mv,
			AsmDeserializerContext context) {
    	
    	 mv.visitCode();
    	 
//       if (flag == Types.NULL) {
//    		return null;
//   	}
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
     	mv.visitIntInsn(BIPUSH, Types.MAP);
     	Label l2 = new Label();
     	mv.visitJumpInsn(IF_ICMPEQ, l2);
     	mv.visitTypeInsn(NEW, "transfer/exceptions/IllegalTypeException");
     	mv.visitInsn(DUP);
     	mv.visitVarInsn(ILOAD, 5);
     	mv.visitIntInsn(BIPUSH, Types.MAP);
     	mv.visitVarInsn(ALOAD, 2);
     	mv.visitMethodInsn(INVOKESPECIAL, "transfer/exceptions/IllegalTypeException", "<init>", "(BBLjava/lang/reflect/Type;)V", false);
     	mv.visitInsn(ATHROW);
		mv.visitLabel(l2);


         Class<?> rawClass = TypeUtils.getRawClass(type);
         if (rawClass == null || rawClass == Map.class || rawClass == Object.class) {

             // new HashMap()
             mv.visitTypeInsn(NEW, AsmUtils.toAsmCls(HashMap.class.getName()));
             mv.visitInsn(DUP);
             mv.visitMethodInsn(INVOKESPECIAL, AsmUtils.toAsmCls(HashMap.class.getName()), "<init>", "()V", false);
             mv.visitVarInsn(ASTORE, 6);

         } else {
             compileCreateMap(type, mv);
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

//       // 循环解析元素
//       for (int i = 0; i < size;i++) {
         mv.visitInsn(ICONST_0);
         mv.visitVarInsn(ISTORE, 8);

         Label l18 = new Label();
         mv.visitLabel(l18);
         mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Opcodes.TOP, Opcodes.INTEGER}, 0, null);
         mv.visitVarInsn(ILOAD, 8);
         mv.visitVarInsn(ILOAD, 7);

         Label l19 = new Label();
         mv.visitJumpInsn(IF_ICMPGE, l19);
         
         
         // 解析Key Value
         Type keyType = null;
     	 Type valueType = null;

         if (type instanceof ParameterizedType) {
             keyType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
             valueType = TypeUtils.getParameterizedType((ParameterizedType) type, 1);
         }
         
         //解析KEY
         int keyLocal = 9;
         Class<?> keyRawClass = TypeUtils.getRawClass(keyType);
         if (keyType == null || keyType == Object.class
 				|| keyRawClass.isInterface()
 				|| Modifier.isAbstract(keyRawClass.getModifiers()) && !keyRawClass.isArray()) {// 使用默认解析器
         	
         	 mv.visitVarInsn(ALOAD, 1);
             mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
             mv.visitVarInsn(ISTORE, 9);

             mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(keyRawClass.getName()) + ";"));
             mv.visitVarInsn(ILOAD, 9);
             mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getDeserializer", "(Ljava/lang/reflect/Type;B)Ltransfer/deserializer/Deserializer;", false);
             mv.visitVarInsn(ASTORE, 10);

             mv.visitVarInsn(ALOAD, 10);
             mv.visitVarInsn(ALOAD, 1);
             mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(keyRawClass.getName()) + ";"));
             mv.visitVarInsn(ILOAD, 9);
             mv.visitVarInsn(ALOAD, 4);
             mv.visitMethodInsn(INVOKEINTERFACE, "transfer/deserializer/Deserializer", "deserialze", "(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/utils/IntegerMap;)Ljava/lang/Object;", true);
             mv.visitVarInsn(ASTORE, 11);
             keyLocal = 11;
         } else {
         	
         	 mv.visitVarInsn(ALOAD, 1);
             mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
             mv.visitVarInsn(ISTORE, 9);
             
             Deserializer keyDeserializer = TransferConfig.getDeserializer(keyType);// Key解析器
             
             mv.visitVarInsn(ALOAD, 0);
             mv.visitVarInsn(ALOAD, 1);
             mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(keyRawClass.getName()) + ";"));
             mv.visitVarInsn(ILOAD, 9);
             mv.visitVarInsn(ALOAD, 4);
             
             MethodVisitor methodVisitor = context.invokeNextDeserialize(null, mv);
             keyDeserializer.compile(keyType, methodVisitor, context);
         	
             mv.visitVarInsn(ASTORE, 10);
             keyLocal = 10;
         }
         
         
         // 解析VALUE
         int valueLocal = keyLocal + 1;
         Class<?> valueRawClass = TypeUtils.getRawClass(valueType);
         if (valueType == null || valueType == Object.class
 				|| valueRawClass.isInterface()
 				|| Modifier.isAbstract(valueRawClass.getModifiers()) && !valueRawClass.isArray()) {// 使用默认解析器
         	
             mv.visitVarInsn(ALOAD, 1);
             mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
             mv.visitVarInsn(ISTORE, keyLocal + 1);

             mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(valueRawClass.getName()) + ";"));
             mv.visitVarInsn(ILOAD, keyLocal + 1);
             mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getDeserializer", "(Ljava/lang/reflect/Type;B)Ltransfer/deserializer/Deserializer;", false);
             mv.visitVarInsn(ASTORE, keyLocal + 2);

             mv.visitVarInsn(ALOAD, keyLocal + 2);
             mv.visitVarInsn(ALOAD, 1);
             mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(valueRawClass.getName()) + ";"));
             mv.visitVarInsn(ILOAD, keyLocal + 1);
             mv.visitVarInsn(ALOAD, 4);
             mv.visitMethodInsn(INVOKEINTERFACE, "transfer/deserializer/Deserializer", "deserialze", "(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/utils/IntegerMap;)Ljava/lang/Object;", true);
             mv.visitVarInsn(ASTORE, keyLocal + 3);
             valueLocal = keyLocal + 3;
         } else {
         	
         	 mv.visitVarInsn(ALOAD, 1);
             mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
             mv.visitVarInsn(ISTORE, keyLocal + 1);
             
             Deserializer keyDeserializer = TransferConfig.getDeserializer(valueType);// Key解析器
             
             mv.visitVarInsn(ALOAD, 0);
             mv.visitVarInsn(ALOAD, 1);
             mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(valueRawClass.getName()) + ";"));
             mv.visitVarInsn(ILOAD, keyLocal + 1);
             mv.visitVarInsn(ALOAD, 4);
             
             MethodVisitor methodVisitor = context.invokeNextDeserialize(null, mv);
             keyDeserializer.compile(valueType, methodVisitor, context);
         	
             mv.visitVarInsn(ASTORE, keyLocal + 2);
             valueLocal = keyLocal + 2;
         }
         
         
         mv.visitVarInsn(ALOAD, 6);
         mv.visitVarInsn(ALOAD, keyLocal);
         mv.visitVarInsn(ALOAD, valueLocal);
         mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
         
         mv.visitInsn(POP);

         mv.visitIincInsn(8, 1);
         mv.visitJumpInsn(GOTO, l18);
         mv.visitLabel(l19);
         
         
         mv.visitFrame(Opcodes.F_CHOP,2, null, 0, null);
         mv.visitVarInsn(ALOAD, 6);
         mv.visitInsn(ARETURN);

         mv.visitMaxs(5, 14);
         mv.visitEnd();
         
	}
    

    private void compileCreateMap(Type type, MethodVisitor mv) {
    	
    	Class<?> rawClass = TypeUtils.getRawClass(type);
    	
    	if (type == AbstractMap.class) {
            rawClass = HashMap.class;
        } else if (type == ConcurrentHashMap.class) {
            rawClass = ConcurrentHashMap.class;
        } else if (rawClass.isAssignableFrom(ConcurrentMap.class)) {
            rawClass = ConcurrentHashMap.class;
        } else if (rawClass.isAssignableFrom(LinkedHashMap.class)) {
            rawClass = LinkedHashMap.class;
        } else if (rawClass.isAssignableFrom(TreeMap.class)) {
            rawClass = TreeMap.class;
        } else if (rawClass.isAssignableFrom(HashMap.class)) {
            rawClass = HashMap.class;
        } else if(rawClass.isInterface() || Modifier.isAbstract(rawClass.getModifiers())) {
        	rawClass = HashMap.class;
        }
    	
    	mv.visitTypeInsn(NEW, AsmUtils.toAsmCls(rawClass.getName()));
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, AsmUtils.toAsmCls(rawClass.getName()), "<init>", "()V", false);
    	
	}


	private Object parseElement(Inputable inputable, Type type, byte byteFlag, IntegerMap referenceMap) {
        Deserializer elementDeserializer = TransferConfig.getDeserializer(type, byteFlag);
        return elementDeserializer.deserialze(inputable, type, byteFlag, referenceMap);
    }


    protected Map<Object, Object> createMap(Type type) {

        if (type == null || type == Map.class || type == Object.class) {
            return new HashMap<Object, Object>();
        }

        if (type == Properties.class) {
            return new Properties();
        }

        if (type == Hashtable.class) {
            return new Hashtable();
        }

        if (type == IdentityHashMap.class) {
            return new IdentityHashMap();
        }

        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap();
        }

        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap();
        }

        if (type == Map.class || type == HashMap.class) {
            return new HashMap();
        }

        if (type == LinkedHashMap.class) {
            return new LinkedHashMap();
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            return createMap(parameterizedType.getRawType());
        }

        Class<?> clazz = (Class<?>) type;
        if (clazz.isInterface()) {
            throw new UnsupportDeserializerTypeException(type);
        }

        try {
            return (Map<Object, Object>) clazz.newInstance();
        } catch (Exception e) {
            throw new UnsupportDeserializerTypeException(type, e);
        }
    }


    public ByteMeta readMeta(Inputable inputable) {

        byte flag = inputable.getByte();
        byte type = TransferConfig.getType(flag);

        if (type != Types.MAP) {
            throw new IllegalTypeException(type, Types.MAP, null);
        }
        // 读取集合的大小
        int size = BitUtils.getInt(inputable);

        ByteMeta byteDataMeta = new ByteMeta();
        byteDataMeta.setComponentSize(size);
        byteDataMeta.setFlag(flag);
        byteDataMeta.setIteratorAble(true);

        return byteDataMeta;
    }


    private static MapDeSerializer instance = new MapDeSerializer();

    public static MapDeSerializer getInstance() {
        return instance;
    }

}
