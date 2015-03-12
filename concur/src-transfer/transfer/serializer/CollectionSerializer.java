package transfer.serializer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.def.TransferConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * 集合编码器
 * Created by Jake on 2015/2/25.
 */
public class CollectionSerializer implements Serializer, Opcodes {


    @Override
    public void serialze(Outputable outputable, Object object, IdentityHashMap referenceMap) {

        if (object == null) {
            NULL_SERIALIZER.serialze(outputable, object, referenceMap);
            return;
        }

        outputable.putByte(Types.COLLECTION);

        Collection collection = (Collection) object;

        // 设置集合大小
        BitUtils.putInt(outputable, collection.size());

        for (Object element : collection) {
        	
            Serializer elementSerializer = TransferConfig.getSerializer(element.getClass());
            
            elementSerializer.serialze(outputable, element, referenceMap);
            
        }

    }

    @Override
    public void compile(Type type, MethodVisitor mv, AsmSerializerContext context) {
    	
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
        mv.visitIntInsn(SIPUSH, (int) Types.COLLECTION);
        mv.visitMethodInsn(INVOKEINTERFACE, "transfer/Outputable", "putByte", "(B)V", true);

        
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, "java/util/Collection");
        mv.visitVarInsn(ASTORE, 4);
        
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "size", "()I", true);
        mv.visitMethodInsn(INVOKESTATIC, "transfer/utils/BitUtils", "putInt", "(Ltransfer/Outputable;I)V", false);
        
        Class<?> elementClass = TypeUtils.getParameterizedClass(type, 0);
        
        Type elementType = null;
        if (type instanceof ParameterizedType) {
        	elementType = TypeUtils.getParameterizedType((ParameterizedType) type, 0);
        }
        
        if (elementClass == null || elementClass == Object.class) {
        
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;", true);
			mv.visitVarInsn(ASTORE, 6);
			Label l7 = new Label();
			mv.visitJumpInsn(GOTO, l7);
			Label l8 = new Label();
			mv.visitLabel(l8);
		//	mv.visitFrame(Opcodes.F_FULL, 7, new Object[] {"transfer/serializer/CollectionSerializer", "transfer/Outputable", "java/lang/Object", "transfer/utils/IdentityHashMap", "java/util/Collection", Opcodes.TOP, "java/util/Iterator"}, 0, new Object[] {});
			mv.visitVarInsn(ALOAD, 6);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
			mv.visitVarInsn(ASTORE, 5);
			
			mv.visitVarInsn(ALOAD, 5);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
			mv.visitMethodInsn(INVOKESTATIC, "transfer/def/TransferConfig", "getSerializer", "(Ljava/lang/Class;)Ltransfer/serializer/Serializer;", false);
			mv.visitVarInsn(ASTORE, 7);
			
			mv.visitVarInsn(ALOAD, 7);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEINTERFACE, "transfer/serializer/Serializer", "serialze", "(Ltransfer/Outputable;Ljava/lang/Object;Ltransfer/utils/IdentityHashMap;)V", true);
			mv.visitLabel(l7);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
			mv.visitJumpInsn(IFNE, l8);
		
        } else {
        	
        	mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;", true);
			mv.visitVarInsn(ASTORE, 6);
			Label l7 = new Label();
			mv.visitJumpInsn(GOTO, l7);
			Label l8 = new Label();
			mv.visitLabel(l8);
		//	mv.visitFrame(Opcodes.F_FULL, 7, new Object[] {"transfer/serializer/CollectionSerializer", "transfer/Outputable", "java/lang/Object", "transfer/utils/IdentityHashMap", "java/util/Collection", Opcodes.TOP, "java/util/Iterator"}, 0, new Object[] {});
			mv.visitVarInsn(ALOAD, 6);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
			mv.visitVarInsn(ASTORE, 5);
			
            
			mv.visitVarInsn(ALOAD, 0);
			
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 3);
			
			// 执行属性预编译
            MethodVisitor methodVisitor = context.invokeNextSerialize(null, mv);

            Serializer fieldSerializer = TransferConfig.getSerializer(elementClass);
            fieldSerializer.compile(elementType, methodVisitor, context);
			
			mv.visitLabel(l7);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
			mv.visitJumpInsn(IFNE, l8);
        	
        }
		
		mv.visitInsn(RETURN);
		
		mv.visitMaxs(4, 8);
		mv.visitEnd();
    	
    }


    private static CollectionSerializer instance = new CollectionSerializer();

    public static CollectionSerializer getInstance() {
        return instance;
    }

}
