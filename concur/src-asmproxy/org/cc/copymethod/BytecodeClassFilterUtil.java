package org.cc.copymethod;
public class BytecodeClassFilterUtil implements IBytecodeContainer{
 
 private ClassNode classNode = null;
 
 /**
  * bytecode class filter utility construction
  * 
  * @param classFile
  * @param op
  * @throws IOException
  */
 public BytecodeClassFilterUtil(final String classFile) throws IOException {
  FileInputStream fis = new FileInputStream(classFile);
  ClassReader cr = new ClassReader(fis);
  BytecodeClassFilter ca = new BytecodeClassFilter(null);
  cr.accept(ca, ClassReader.EXPAND_FRAMES);
  if (fis != null) {
   fis.close();
  }
 }
 
 /**
  * bytecode class filter utility construction
  * 
  * @param classFile
  * @param op
  * @throws IOException
  */
 public BytecodeClassFilterUtil(File classFile) throws IOException {
  FileInputStream fis = new FileInputStream(classFile);
  ClassReader cr = new ClassReader(fis);
  BytecodeClassFilter ca = new BytecodeClassFilter(null);
  cr.accept(ca, ClassReader.EXPAND_FRAMES);
  if (fis != null) {
   fis.close();
  }
 }
 
 /**
  * get a specified class node instance for current bytecode class filter utility
  * 
  * @return
  */
 public ClassNode getClassNode() {
  return this.classNode;
 }
 
 /**
  * get a specified field node by a specified name pattern and description pattern
  * 
  * @param name
  * @return
  */
 @SuppressWarnings("unchecked")
 public List<FieldNode> getFieldNode(String namePattern, String descPattern) {
  List<FieldNode> returnNodes = new ArrayList<FieldNode>();
  List fields = this.classNode.fields;
  if (fields != null) {
   for (Object ofield : fields) {
    FieldNode field = (FieldNode) ofield;
    boolean blnNameMatch = true;
    boolean blnDescMatch = true;
    if (namePattern != null) {
     blnNameMatch = Pattern.matches(namePattern, field.name);
    }
    if (descPattern != null) {
     blnDescMatch = Pattern.matches(descPattern, field.desc);
    }
    if (blnNameMatch && blnDescMatch) {
     returnNodes.add(field);
    }
   }
  }
  return returnNodes;
 }
 
 /**
  * get a specified method name or a list of them.
  * 
  * @param name
  * @param description
  * @return
  */
 @SuppressWarnings("unchecked")
 public List<MethodNode> getMethodNode(String namePattern, String descPattern) {
  List<MethodNode> returnNodes = new ArrayList<MethodNode>();
  List methods = this.classNode.methods;
  if (methods != null) {
   for (Object omethod : methods) {
    MethodNode method = (MethodNode) omethod;
    boolean blnNameMatch = true;
    boolean blnDescMatch = true;
    if (namePattern != null) {
     blnNameMatch = Pattern.matches(namePattern, method.name);
    }
    if (descPattern != null) {
     blnDescMatch = Pattern.matches(descPattern, method.desc);
    }
    if (blnNameMatch && blnDescMatch) {
     returnNodes.add(method);
    }
   }
  }
  return returnNodes;
 }

 /**
  * get all of the field descriptions for a specified class
  * 
  * @return
  */
 @SuppressWarnings("unchecked")
 public List<String> getFieldDescription() {
  List<String> descList = new ArrayList<String>();
  List fields = this.classNode.fields;
  if (fields != null) {
   for (Object ofield : fields) {
    FieldNode field = (FieldNode) ofield;
    StringBuilder sb = new StringBuilder();
    sb.append(field.name).append(":").append(field.desc);
    descList.add(sb.toString());
   }
  }
  return descList;
 }
 
 /**
  * get all of the method list for a specified class
  * 
  * @return
  */
 @SuppressWarnings("unchecked")
 public List<String> getMethodDescription() {
  List<String> descList = new ArrayList<String>();
  List methods = this.classNode.methods;
  if (methods != null) {
   for (Object omethod : methods) {
    MethodNode method = (MethodNode) omethod;
    StringBuilder sb = new StringBuilder();
    sb.append(method.name).append(":").append(method.desc);
    descList.add(sb.toString());
   }
  }
  return descList;
 }
 
 /**
  * bytecode class filter extend from class adpater class.
  * 
  */
 class BytecodeClassFilter extends ClassAdapter {

  // construction call for current class
  public BytecodeClassFilter(final ClassVisitor cv) {
   super(new ClassNode() {
    public void visitEnd() {
     if (cv != null) {
      accept(cv);
     }
    }
   });
  }

  // execute the next operation after this visit ending
  public void visitEnd() {
   classNode = (ClassNode) cv;
  }

 }

}

------------------------------------------------------------------------------------------------

构造调用函数，实现方法“转移”功能:

public void addMethodToClass(String src, String des, String combine, String nameFilter, String descFilter)
   throws IOException {
  BytecodeClassFilterUtil util = new BytecodeClassFilterUtil(src);
  List<MethodNode> methods = util.getMethodNode(nameFilter, descFilter);
  
  // visitor current class
  if (methods.size() == 0) {
   System.out.println("ERROR: No method is chosen out by the filter.");
  } else {
   ClassWriter cw = new ClassWriter(0);
   BytecodeClassMethodAdder adder = new BytecodeClassMethodAdder(cw, methods);
   FileInputStream fis = new FileInputStream(des);
   ClassReader cr = new ClassReader(fis);
   cr.accept(adder, ClassReader.EXPAND_FRAMES); // need to expand frames for current end user
   if (fis != null) {
    fis.close();
   }
   
   // convert the specified method into current class
   byte[] bytearray = cw.toByteArray();
   FileOutputStream fos = new FileOutputStream(combine);
   fos.write(bytearray);
   fos.flush();
   fos.close();
  }
 }