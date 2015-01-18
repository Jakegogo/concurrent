package dbcache.support.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import dbcache.utils.AsmUtils;

/**
 * (动态)生成替换类
 * <br/>继承源类
 *
 * @author Jake
 * @date 2014年9月6日上午12:06:47
 */
public class EntityClassReplaceAdapter extends ClassVisitor implements Opcodes {

	/** 构造方法名常量 */
	public static final String INIT = "<init>";

	/** 真实对象的属性名 */
	public static final String REAL_OBJECT = "obj";

	/** 处理类对象的属性名 */
	public static final String HANDLER_OBJECT = "handler";

	/**
	 * 切面方法重写器
	 */
	private AbstractAsmMethodReplaceAspect methodAspect;

	/**
	 * ClassWriter
	 */
	private ClassWriter classWriter;

	/**
	 * 原始类
	 */
	private Class<?> originalClass;

	/**
	 * 代理类名
	 */
	private String enhancedClassName;

	/**
	 * 构造方法
	 *
	 * @param enhancedClassName 代理类类名
	 * @param targetClass 被代理类
	 * @param writer  ClassWriter
	 */
	public EntityClassReplaceAdapter(String enhancedClassName, Class<?> targetClass,
			ClassWriter writer) {
		this(enhancedClassName, targetClass, writer, new AbstractAsmMethodReplaceAspect() {});
	}

	/**
	 * 构造方法
	 * @param enhancedClassName 代理类类名
	 * @param targetClass 被代理类
	 * @param writer ClassWriter
	 * @param methodAspect 方法切面修改器
	 */
	public EntityClassReplaceAdapter(String enhancedClassName, Class<?> targetClass,
			ClassWriter writer, AbstractAsmMethodReplaceAspect methodAspect) {
		super(Opcodes.ASM4, writer);
		this.classWriter = writer;
		this.enhancedClassName = enhancedClassName;
		this.originalClass = targetClass;
		this.methodAspect = methodAspect;
	}



	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		cv.visit(version, Opcodes.ACC_PUBLIC,
				AsmUtils.toAsmCls(enhancedClassName), signature, name,
				interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		MethodVisitor mWriter = super.visitMethod(access, name, desc, signature, exceptions);
		
		
		mWriter = this.methodAspect.doBefore(originalClass, mWriter, null, 1, name, access, desc);
		
		this.methodAspect.doAfter(originalClass, mWriter, null, 1, name, access, desc);
		
		return mWriter;
	}


	@Override
	public void visitEnd() {
				
	}

}
