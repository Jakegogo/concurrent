package basesource.convertor.utils;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.Attribute;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.commons.EmptyVisitor;

/**
 * 字节码访问器
 * 
 * @author JY253
 * 
 */
class ClassMetaVisitor implements ClassVisitor, ClassMeta {

	private String className;

	private int version;

	private int access;

	private String superName;

	private String[] interfaces;

	private String signature;

	private byte[] bytes;
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.className = name.replaceAll("/", "\\.");
		this.version = version;
		this.access = access;
		this.superName = superName;
		this.signature = signature;
		this.interfaces = interfaces;
	}

	@Override
	public void visitSource(String source, String debug) {
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return new EmptyVisitor();
	}

	@Override
	public void visitAttribute(Attribute attr) {

	}

	@Override
	public void visitInnerClass(String name, String outerName,
			String innerName, int access) {
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return new EmptyVisitor();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return new EmptyVisitor();
	}

	@Override
	public void visitEnd() {

	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public int getAccess() {
		return access;
	}

	@Override
	public String getSuperName() {
		return superName;
	}

	@Override
	public String[] getInterfaces() {
		return interfaces;
	}

	@Override
	public String getSignature() {
		return signature;
	}

	@Override
	public byte[] getBytes() {
		return this.bytes;
	}

	void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}