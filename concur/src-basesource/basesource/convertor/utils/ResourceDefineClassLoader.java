package basesource.convertor.utils;

import dbcache.DbCacheService;

import java.security.PrivilegedAction;

/**
 * Asm字节码类加载器
 * @author Jake
 * @date 2014年11月1日上午2:04:32
 */
public class ResourceDefineClassLoader extends ClassLoader {

    private static java.security.ProtectionDomain DOMAIN;

    static {
        DOMAIN = (java.security.ProtectionDomain) java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                return ResourceDefineClassLoader.class.getProtectionDomain();
            }
        });
    }

    public ResourceDefineClassLoader(){
        super(getParentClassLoader());
    }

    public ResourceDefineClassLoader(ClassLoader parent){
        super (parent);
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

        Class<?> clazz = defineClass(name, classBytes, 0, classBytes.length);

        if(clazz != null){
            super.resolveClass(clazz);
        }

        return clazz;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {



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
