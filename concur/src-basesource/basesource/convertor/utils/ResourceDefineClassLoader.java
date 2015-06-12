package basesource.convertor.utils;

import dbcache.DbCacheService;

import java.io.File;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

/**
 * Asm字节码类加载器
 * @author Jake
 * @date 2014年11月1日上午2:04:32
 */
public class ResourceDefineClassLoader extends ClassLoader {

    private static java.security.ProtectionDomain DOMAIN;

    private String basePath;

    private Map<String, Class<?>> loadedClass = new HashMap<String, Class<?>>();

    static {
        DOMAIN = (java.security.ProtectionDomain) java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                return ResourceDefineClassLoader.class.getProtectionDomain();
            }
        });
    }

    public ResourceDefineClassLoader(String basePath){
        super(getParentClassLoader());
        this.basePath = basePath;
    }

    public ResourceDefineClassLoader(ClassLoader parent, String basePath){
        super (parent);
        this.basePath = basePath;
    }

    static ClassLoader getParentClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                contextClassLoader.loadClass(DbCacheService.class.getName());
                return contextClassLoader;
            } catch (ClassNotFoundException e) {
                // skip
            }
        }
        return DbCacheService.class.getClassLoader();
    }

    public Class<?> defineClass(String name, byte[] b) throws ClassFormatError {
        return defineClass(name, b, 0, b.length, DOMAIN);
    }


    /**
     * 加载类
     * @param name 类名
     * @param classBytes 对应.class文件字节数组
     * @return Class<?>
     */
    public Class<?> loadClass(String name, byte[] classBytes) {
        if(classBytes == null || classBytes.length == 0){
            return null;
        }

        Class<?> clazz = loadedClass.get(name);
        if (clazz != null) {
            return clazz;
        }

        clazz = defineClass(name, classBytes, 0, classBytes.length);

        if(clazz != null){
            super.resolveClass(clazz);
        }

        loadedClass.put(name, clazz);
        return clazz;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {

        String path = name.replace('.', '/').concat(".class");
        File classFile = new File(basePath + File.separator + path);

        ClassMeta classMeta = ClassMetaUtil.getClassMeta(classFile);
        Class<?> clazz = loadClass(classMeta.getClassName(), classMeta.getBytes());

        if (clazz != null) {
            return clazz;
        }

        return super.findClass(name);
    }

    public boolean isExternalClass(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();

        if (classLoader == null) {
            return false;
        }

        ClassLoader current = this;
        while (current != null) {
            if (current == classLoader) {
                return false;
            }

            current = current.getParent();
        }

        return true;
    }

}
