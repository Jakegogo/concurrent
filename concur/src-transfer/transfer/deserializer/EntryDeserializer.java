package transfer.deserializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.core.DeserialContext;
import transfer.core.ParseStackTrace;
import transfer.def.TransferConfig;
import transfer.utils.TypeUtils;
import utils.enhance.asm.util.AsmUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map.Entry解析器
 * <br/>尽量指定泛型类型,可提升解析性能
 * Created by Jake on 2015/2/24.
 */
public class EntryDeserializer implements Deserializer, Opcodes {


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, DeserialContext context) {

        ParseStackTrace stack = context.nextStackTrace(type);

        Type keyType = null;
        Type valueType = null;

        if (type instanceof ParameterizedType) {
            keyType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
            valueType = TypeUtils.getParameterizedType((ParameterizedType) type, 1);
        }

        context.next(stack, "key", keyType);
        // 读取元素类型
        byte keyFlag = inputable.getByte();
        final Object key = parseElement(inputable, keyType, keyFlag, context);

        context.next(stack, "value", keyType);
        byte valueFlag = inputable.getByte();
        final Object value = parseElement(inputable, valueType, valueFlag, context);

        return (T) new UnmodificationEntry(key, value);
    }

    
    @Override
	public void compile(Type type, MethodVisitor mv,
			AsmDeserializerContext context) {
    	
    	Type keyType = null;
    	Type valueType = null;

        if (type instanceof ParameterizedType) {
            keyType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
            valueType = TypeUtils.getParameterizedType((ParameterizedType) type, 1);
        }
        
        
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
        
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
        mv.visitVarInsn(ISTORE, 5);
        
        //解析KEY
        int keyLocal = 5;
        Class<?> keyRawClass = TypeUtils.getRawClass(keyType);
        if (keyType == null || keyType == Object.class
				|| keyRawClass.isInterface()
				|| Modifier.isAbstract(keyRawClass.getModifiers()) && !keyRawClass.isArray()) {// 使用默认解析器
        	
            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(keyRawClass.getName()) + ";"));
            mv.visitVarInsn(ILOAD, 5);
            mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getDeserializer", "(Ljava/lang/reflect/Type;B)Ltransfer/deserializer/Deserializer;", false);
            mv.visitVarInsn(ASTORE, 6);

            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(keyRawClass.getName()) + ";"));
            mv.visitVarInsn(ILOAD, 5);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "transfer/deserializer/Deserializer", "deserialze", "(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/core/DeserialContext;)Ljava/lang/Object;", true);
            mv.visitVarInsn(ASTORE, 7);
            keyLocal = 7;
        } else {
            
            Deserializer keyDeserializer = TransferConfig.getDeserializer(keyType);// Key解析器
            
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(keyRawClass.getName()) + ";"));
            mv.visitVarInsn(ILOAD, 5);
            mv.visitVarInsn(ALOAD, 4);
            
            MethodVisitor methodVisitor = context.invokeNextDeserialize(null, mv);
            keyDeserializer.compile(keyType, methodVisitor, context);
        	
            mv.visitVarInsn(ASTORE, 6);
            keyLocal = 6;
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
            mv.visitMethodInsn(INVOKEINTERFACE, "transfer/deserializer/Deserializer", "deserialze", "(Ltransfer/Inputable;Ljava/lang/reflect/Type;BLtransfer/core/DeserialContext;)Ljava/lang/Object;", true);
            mv.visitVarInsn(ASTORE, keyLocal + 3);
            valueLocal = keyLocal + 3;
        } else {
        	
        	mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Inputable", "getByte", "()B", true);
            mv.visitVarInsn(ISTORE, keyLocal + 1);
            
            Deserializer keyDeserializer = TransferConfig.getDeserializer(valueType);// Key解析器
            MethodVisitor methodVisitor = context.invokeNextDeserialize(null, mv);
            
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(org.objectweb.asm.Type.getType("L" + AsmUtils.toAsmCls(valueRawClass.getName()) + ";"));
            mv.visitVarInsn(ILOAD, keyLocal + 1);
            mv.visitVarInsn(ALOAD, 4);
            
            keyDeserializer.compile(valueType, methodVisitor, context);
        	
            mv.visitVarInsn(ASTORE, keyLocal + 2);
            valueLocal = keyLocal + 2;
        }
        
        
        mv.visitTypeInsn(NEW, "transfer/deserializer/EntryDeserializer$UnmodificationEntry");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, keyLocal);
        mv.visitVarInsn(ALOAD, valueLocal);
        mv.visitMethodInsn(INVOKESPECIAL, "transfer/deserializer/EntryDeserializer$UnmodificationEntry", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
        mv.visitInsn(ARETURN);
        
        mv.visitMaxs(5, valueLocal);
        mv.visitEnd();
	}
    

    private Object parseElement(Inputable inputable, Type type, byte byteFlag, DeserialContext context) {
        Deserializer elementDeserializer = TransferConfig.getDeserializer(type, byteFlag);
        return elementDeserializer.deserialze(inputable, type, byteFlag, context);
    }


    /**
     * 不可以更改的Entry
     * @param <K>
     * @param <V>
     */
    public static class UnmodificationEntry<K, V> implements Map.Entry<K, V> {

        private K key;

        private V value;

        public UnmodificationEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public final int hashCode() {
            return (key==null   ? 0 : key.hashCode()) ^
                    (value==null ? 0 : value.hashCode());
        }
    }


    private static EntryDeserializer instance = new EntryDeserializer();

    public static EntryDeserializer getInstance() {
        return instance;
    }

}
