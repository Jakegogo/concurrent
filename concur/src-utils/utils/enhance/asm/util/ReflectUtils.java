/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package utils.enhance.asm.util;

import org.objectweb.asm.Type;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * @version $Id: ReflectUtils.java,v 1.30 2009/01/11 19:47:49 herbyderby Exp $
 */
public class ReflectUtils {
    private ReflectUtils() { }
    
    private static final Map primitives = new HashMap(8);
    private static final Map transforms = new HashMap(8);
    private static final ClassLoader defaultLoader = ReflectUtils.class.getClassLoader();
    private static Method DEFINE_CLASS;
    private static final ProtectionDomain PROTECTION_DOMAIN;
    
    static {
        PROTECTION_DOMAIN = (ProtectionDomain)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return ReflectUtils.class.getProtectionDomain();
            }
        });
        
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    Class loader = Class.forName("java.lang.ClassLoader"); // JVM crash w/o this
                    DEFINE_CLASS = loader.getDeclaredMethod("defineClass",
                                                            new Class[]{ String.class,
                                                                         byte[].class,
                                                                         Integer.TYPE,
                                                                         Integer.TYPE,
                                                                         ProtectionDomain.class });
                    DEFINE_CLASS.setAccessible(true);
                } catch (ClassNotFoundException e) {
                    throw new CodeGenerationException(e);
                } catch (NoSuchMethodException e) {
                    throw new CodeGenerationException(e);
                }
                return null;
            }
        });
    }
        
    private static final String[] CGLIB_PACKAGES = {
        "java.lang",
    };
        
    static {
        primitives.put("byte", Byte.TYPE);
        primitives.put("char", Character.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("short", Short.TYPE);
        primitives.put("boolean", Boolean.TYPE);
            
        transforms.put("byte", "B");
        transforms.put("char", "C");
        transforms.put("double", "D");
        transforms.put("float", "F");
        transforms.put("int", "I");
        transforms.put("long", "J");
        transforms.put("short", "S");
        transforms.put("boolean", "Z");
    }
        
    public static Type[] getExceptionTypes(Member member) {
        if (member instanceof Method) {
            return TypeUtils.getTypes(((Method)member).getExceptionTypes());
        } else if (member instanceof Constructor) {
            return TypeUtils.getTypes(((Constructor)member).getExceptionTypes());
        } else {
            throw new IllegalArgumentException("Cannot get exceptions types of a field");
        }
    }
        
    public static Signature getSignature(Member member) {
        if (member instanceof Method) {
            return new Signature(member.getName(), Type.getMethodDescriptor((Method)member));
        } else if (member instanceof Constructor) {
            Type[] types = TypeUtils.getTypes(((Constructor)member).getParameterTypes());
            return new Signature(Constants.CONSTRUCTOR_NAME,
                                 Type.getMethodDescriptor(Type.VOID_TYPE, types));
                
        } else {
            throw new IllegalArgumentException("Cannot get signature of a field");
        }
    }
        
    public static Constructor findConstructor(String desc) {
        return findConstructor(desc, defaultLoader);
    }
        
    public static Constructor findConstructor(String desc, ClassLoader loader) {
        try {
            int lparen = desc.indexOf('(');
            String className = desc.substring(0, lparen).trim();
            return getClass(className, loader).getConstructor(parseTypes(desc, loader));
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }
        
    public static Method findMethod(String desc) {
        return findMethod(desc, defaultLoader);
    }
        
    public static Method findMethod(String desc, ClassLoader loader) {
        try {
            int lparen = desc.indexOf('(');
            int dot = desc.lastIndexOf('.', lparen);
            String className = desc.substring(0, dot).trim();
            String methodName = desc.substring(dot + 1, lparen).trim();
            return getClass(className, loader).getDeclaredMethod(methodName, parseTypes(desc, loader));
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }
      
    public static Class[] parseTypes(String desc) throws ClassNotFoundException {
    	return parseTypes(desc, defaultLoader);
    }
    
    public static Class[] parseTypes(String desc, ClassLoader loader) throws ClassNotFoundException {
    	final Type[] argumentsType = Type.getArgumentTypes(desc);
    	Class[] types = new Class[argumentsType.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = getClass(argumentsType[i].getClassName(), loader);
        }
        return types;
    }
        
    private static Class getClass(String className, ClassLoader loader) throws ClassNotFoundException {
        return getClass(className, loader, CGLIB_PACKAGES);
    }
        
    private static Class getClass(String className, ClassLoader loader, String[] packages) throws ClassNotFoundException {
        String save = className;
        int dimensions = 0;
        int index = 0;
        while ((index = className.indexOf("[]", index) + 1) > 0) {
            dimensions++;
        }
        StringBuilder brackets = new StringBuilder(className.length() - dimensions);
        for (int i = 0; i < dimensions; i++) {
            brackets.append('[');
        }
        className = className.substring(0, className.length() - 2 * dimensions);
            
        String prefix = (dimensions > 0) ? brackets + "L" : "";
        String suffix = (dimensions > 0) ? ";" : "";
        try {
            return Class.forName(prefix + className + suffix, false, loader);
        } catch (ClassNotFoundException ignore) { }
        for (String aPackage : packages) {
            try {
                return Class.forName(prefix + aPackage + '.' + className + suffix, false, loader);
            } catch (ClassNotFoundException ignore) {
            }
        }
        if (dimensions == 0) {
            Class c = (Class)primitives.get(className);
            if (c != null) {
                return c;
            }
        } else {
            String transform = (String)transforms.get(className);
            if (transform != null) {
                try {
                    return Class.forName(brackets + transform, false, loader);
                } catch (ClassNotFoundException ignore) { }
            }
        }
        throw new ClassNotFoundException(save);
    }
        
        
    public static Object newInstance(Class type) {
        return newInstance(type, Constants.EMPTY_CLASS_ARRAY, null);
    }
        
    public static Object newInstance(Class type, Class[] parameterTypes, Object[] args) {
        return newInstance(getConstructor(type, parameterTypes), args);
    }
        
    public static Object newInstance(final Constructor cstruct, final Object[] args) {
            
        boolean flag = cstruct.isAccessible();
        try {
            cstruct.setAccessible(true);
            return cstruct.newInstance(args);
        } catch (InstantiationException e) {
            throw new CodeGenerationException(e);
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new CodeGenerationException(e.getTargetException());
        } finally {
            cstruct.setAccessible(flag);
        }
                
    }
        
    public static Constructor getConstructor(Class type, Class[] parameterTypes) {
        try {
            Constructor constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static String[] getNames(Class[] classes)
    {
        if (classes == null)
            return null;
        String[] names = new String[classes.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = classes[i].getName();
        }
        return names;
    }
        
    public static Class[] getClasses(Object[] objects) {
        Class[] classes = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            classes[i] = objects[i].getClass();
        }
        return classes;
    }
        
    public static Method findNewInstance(Class iface) {
        Method m = findInterfaceMethod(iface);
        if (!m.getName().equals("newInstance")) {
            throw new IllegalArgumentException(iface + " missing newInstance method");
        }
        return m;
    }

    public static Method[] getPropertyMethods(PropertyDescriptor[] properties, boolean read, boolean write) {
        Set methods = new HashSet();
        for (PropertyDescriptor pd : properties) {
            if (read) {
                methods.add(pd.getReadMethod());
            }
            if (write) {
                methods.add(pd.getWriteMethod());
            }
        }
        methods.remove(null);
        return (Method[])methods.toArray(new Method[methods.size()]);
    }
        
    public static PropertyDescriptor[] getBeanProperties(Class type) {
        return getPropertiesHelper(type, true, true);
    }
        
    public static PropertyDescriptor[] getBeanGetters(Class type) {
        return getPropertiesHelper(type, true, false);
    }
        
    public static PropertyDescriptor[] getBeanSetters(Class type) {
        return getPropertiesHelper(type, false, true);
    }
        
    private static PropertyDescriptor[] getPropertiesHelper(Class type, boolean read, boolean write) {
        try {
            BeanInfo info = Introspector.getBeanInfo(type, Object.class);
            PropertyDescriptor[] all = info.getPropertyDescriptors();
            if (read && write) {
                return all;
            }
            List properties = new ArrayList(all.length);
            for (PropertyDescriptor pd : all) {
                if ((read && pd.getReadMethod() != null) ||
                        (write && pd.getWriteMethod() != null)) {
                    properties.add(pd);
                }
            }
            return (PropertyDescriptor[])properties.toArray(new PropertyDescriptor[properties.size()]);
        } catch (IntrospectionException e) {
            throw new CodeGenerationException(e);
        }
    }
        
        
        
    public static Method findDeclaredMethod(final Class type,
                                            final String methodName, final Class[] parameterTypes)
    throws NoSuchMethodException {
                        
        Class cl = type;
        while (cl != null) {
            try {
                return cl.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                cl = cl.getSuperclass();
            }
        }
        throw new NoSuchMethodException(methodName);
            
    }
        
    public static List addAllMethods(final Class type, final List list) {
            
            
        list.addAll(java.util.Arrays.asList(type.getDeclaredMethods()));
        Class superclass = type.getSuperclass();
        if (superclass != null) {
            addAllMethods(superclass, list);
        }
        Class[] interfaces = type.getInterfaces();
        for (Class anInterface : interfaces) {
            addAllMethods(anInterface, list);
        }
            
        return list;
    }
        
    public static List addAllInterfaces(Class type, List list) {
        Class superclass = type.getSuperclass();
        if (superclass != null) {
            list.addAll(Arrays.asList(type.getInterfaces()));
            addAllInterfaces(superclass, list);
        }
        return list;
    }
        
        
    public static Method findInterfaceMethod(Class iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        Method[] methods = iface.getDeclaredMethods();
        if (methods.length != 1) {
            throw new IllegalArgumentException("expecting exactly 1 method in " + iface);
        }
        return methods[0];
    }
        
    public static Class defineClass(String className, byte[] b, ClassLoader loader) throws Exception {
        Object[] args = new Object[]{className, b, 0, b.length, PROTECTION_DOMAIN };
        Class c = (Class)DEFINE_CLASS.invoke(loader, args);
        // Force static initializers to run.
        Class.forName(className, true, loader);
        return c;
    }
        
    public static int findPackageProtected(Class[] classes) {
        for (int i = 0; i < classes.length; i++) {
            if (!Modifier.isPublic(classes[i].getModifiers())) {
                return i;
            }
        }
        return 0;
    }

    
    // used by MethodInterceptorGenerated generated code
    public static Method[] findMethods(String[] namesAndDescriptors, Method[] methods)
    {
        Map map = new HashMap();
        for (Method method : methods) {
            map.put(method.getName() + Type.getMethodDescriptor(method), method);
        }
        Method[] result = new Method[namesAndDescriptors.length / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Method)map.get(namesAndDescriptors[i * 2] + namesAndDescriptors[i * 2 + 1]);
            if (result[i] == null) {
                // TODO: error?
            }
        }
        return result;
    }
}


/**
 * @version $Id: CodeGenerationException.java,v 1.3 2004/06/24 21:15:21 herbyderby Exp $
 */
class CodeGenerationException extends RuntimeException {
    private Throwable cause;

    public CodeGenerationException(Throwable cause) {
        super(cause.getClass().getName() + "-->" + cause.getMessage());
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }
}

