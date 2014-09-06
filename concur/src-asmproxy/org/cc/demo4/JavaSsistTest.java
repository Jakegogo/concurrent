package org.cc.demo4;

import org.cc.demo2.Entity;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class JavaSsistTest {
	
	public static void main(String[] args) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException {
		ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("org.cc.demo2.Entity");
        CtMethod ctMethod = ctClass.getDeclaredMethod("setNum");
        ctMethod.insertAfter("System.out.println(\"this is a new method\");");
        Class rsCls = ctClass.toClass();
        
        Entity entity = (Entity) rsCls.newInstance();
		entity.setNum(2);
        
		Entity entity1 = new Entity();
		entity1.setNum(2);
	}
	
}
