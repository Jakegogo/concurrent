package org.cc.copymethod;
public class BytecodeClassMethodAdder extends ClassAdapter {
 
 // method nodes for appending operation
 private final List<MethodNode> methodNodesToAppend;
 
 /**
  * Construction for bytecode class method adder operation
  * 
  * @param cv
  * @param methodNodes
  */
 public BytecodeClassMethodAdder(final ClassVisitor cv, List<MethodNode> methodNodes) {
  super(cv);
  
  // all method nodes needed to append for current class
  this.methodNodesToAppend = methodNodes;
 }
 
 /**
  * visit end of this adapter for current class
  * 
  */
 @SuppressWarnings("unchecked")
 public void visitEnd() {
  for (MethodNode mn : this.methodNodesToAppend) {
   List exceptions = mn.exceptions;
   String [] earray = null;
   if (exceptions.size() > 0){
    earray = new String[exceptions.size()];
    for (int i=0; i<exceptions.size(); i++){
     String exception = (String)exceptions.get(i);
     earray[i] = exception;
    }
   }
   mn.accept(cv);    //add this method node to the visitor operation
  }
  
  // overload the visiting operation
  super.visitEnd();
 }
 
}

-----------------------------------------------------------------------------------------------------------

然后构造从另外一个类B中抽取方法的操作类：

