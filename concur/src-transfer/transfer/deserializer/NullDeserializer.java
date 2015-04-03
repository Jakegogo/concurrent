package transfer.deserializer;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * NULL解析器
 * Created by Jake on 2015/2/24.
 */
public class NullDeserializer implements Deserializer, Opcodes {

	
    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {
        return null;
    }

    
    @Override
	public void compile(Type type, MethodVisitor mv,
			AsmDeserializerContext context) {
    	
    	mv.visitCode();
    	mv.visitInsn(ACONST_NULL);
    	mv.visitInsn(ARETURN);
    	mv.visitMaxs(1, 5);
    	mv.visitEnd();
    	
	}

    private static NullDeserializer instance = new NullDeserializer();

    public static NullDeserializer getInstance() {
        return instance;
    }

}
