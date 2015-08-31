package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.SerialContext;

import java.lang.reflect.Type;

/**
 * 类型编码接口 Created by Jake on 2015/2/23.
 */
public interface Serializer {

	/**
	 * 编码方法
	 *  @param outputable
	 *            输出接口
	 * @param object
	 *            目标对象
	 * @param context
	 */
	void serialze(Outputable outputable, Object object,
			SerialContext context);

	/**
	 * 预编译编码方法
	 * 
	 * @param type Type
	 * @param mw
	 *            serialze方法MethodVisitor
	 * @param context
	 */
	void compile(Type type, MethodVisitor mw, AsmSerializerContext context);

	/**
	 * null编码器
	 */
	NullSerializer NULL_SERIALIZER = NullSerializer.getInstance();

}
