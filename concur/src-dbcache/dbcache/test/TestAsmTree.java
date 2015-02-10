package dbcache.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javassist.bytecode.Opcode;
import net.sf.cglib.core.TypeUtils;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public class TestAsmTree {
	/** 
	     * @param args 
	     * @author lihzh 
	     * @date 2012-4-21 下午10:17:22 
	     */ 
	    public static void main(String[] args) { 
	    	
	    	TypeUtils.getBoxedType(null);
	    	
	        try { 
	            ClassReader reader = new ClassReader( 
	                    "dbcache.test.Entity"); 
	            ClassNode cn = new ClassNode(); 
	            reader.accept(cn, 0); 
	            List<MethodNode> methodList = cn.methods; 
	            for (MethodNode md : methodList) {
	            	System.out.println("===================");
	                System.out.println(md.name); 
	                System.out.println(md.access); 
	                System.out.println(md.desc); 
	                System.out.println(md.signature);
	                
	                System.out.println("arguments: ");
	                
	                final Type[] arguments = Type.getArgumentTypes(md.desc);
	        		for (Type argType : arguments) {
	        			try {
							System.out.println(argType.getClassName() + " : " + Class.forName(argType.getClassName()));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
	        		}
	        		
	                List<LocalVariableNode> lvNodeList = md.localVariables; 
	                for (LocalVariableNode lvn : lvNodeList) { 
	                	System.out.println("----------------------");
	                    System.out.println("Local name: " + lvn.name);
	                    System.out.println("Local name: " + lvn.start.getLabel()); 
	                    System.out.println("Local name: " + lvn.desc); 
	                    System.out.println("Local name: " + lvn.signature); 
	                }
	                
	                if (md.attrs != null) {
		                for(Attribute attr : (List<Attribute>)md.attrs) {
		                	System.out.println("attr: " + attr.toString());
		                }
	                }
	                
	                if (md.instructions != null) {
	                	for (ListIterator<AbstractInsnNode> it = md.instructions.iterator();it.hasNext();) {
	                		AbstractInsnNode node = it.next();
	                		System.out.println("instruction: " + node.toString());
	                		if (node instanceof FieldInsnNode) {
	                			FieldInsnNode fieldNode = ((FieldInsnNode) node);
	                			if (fieldNode.getOpcode() == Opcode.PUTFIELD) {
	                				System.out.println("put field : " + fieldNode.name);
	                			}
	                		}
	                	}
	                }
	                
	                
	                Iterator<AbstractInsnNode> instraIter = md.instructions.iterator(); 
	                while (instraIter.hasNext()) { 
	                    AbstractInsnNode abi = instraIter.next(); 
	                    if (abi instanceof LdcInsnNode) { 
	                        LdcInsnNode ldcI = (LdcInsnNode) abi; 
	                        System.out.println("LDC node value: " + ldcI.cst); 
	                    } 
	                }
	                System.out.println("===================");
	            } 
	            MethodVisitor mv = cn.visitMethod(Opcodes.AALOAD, "<init>", Type 
	                    .getType(String.class).toString(), null, null); 
	            mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(String.class), "str", Type 
	                    .getType(String.class).toString()); 
	            System.out.println(cn.name); 
	            List<FieldNode> fieldList = cn.fields; 
	            for (FieldNode fieldNode : fieldList) { 
	                System.out.println("Field name: " + fieldNode.name); 
	                System.out.println("Field desc: " + fieldNode.desc); 
	                System.out.println("Filed value: " + fieldNode.value); 
	                System.out.println("Filed access: " + fieldNode.access); 
	                if (fieldNode.visibleAnnotations != null) { 
	                    for (AnnotationNode anNode : (List<AnnotationNode>)fieldNode.visibleAnnotations) { 
	                        System.out.println(anNode.desc); 
	                    } 
	                } 
	            } 
	        } catch (IOException e) { 
	            e.printStackTrace(); 
	        } catch (SecurityException e) { 
	            e.printStackTrace(); 
	        } catch (IllegalArgumentException e) { 
	            e.printStackTrace(); 
	        } 
	    } 
}

