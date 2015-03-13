package transfer.deserializer;

import transfer.Inputable;
import transfer.compile.AsmDeserializerContext;
import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

import org.objectweb.asm.MethodVisitor;

/**
 * NULL解析器
 * Created by Jake on 2015/2/24.
 */
public class NullDeserializer implements Deserializer {

	
    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {
        return null;
    }

    
    @Override
	public void compile(Type type, MethodVisitor mw,
			AsmDeserializerContext context) {
    	
	}

    private static NullDeserializer instance = new NullDeserializer();

    public static NullDeserializer getInstance() {
        return instance;
    }

}
