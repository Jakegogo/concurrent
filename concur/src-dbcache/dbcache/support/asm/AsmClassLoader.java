package dbcache.support.asm;

import dbcache.DbCacheService;

import java.security.PrivilegedAction;

/**
 * Asm字节码类加载器
 * @author Jake
 * @date 2014年11月1日上午2:04:32
 */
public class AsmClassLoader extends ClassLoader {

    private static java.security.ProtectionDomain DOMAIN;

    static {
        DOMAIN = (java.security.ProtectionDomain) java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                return AsmClassLoader.class.getProtectionDomain();
            }
        });
    }

    public AsmClassLoader(){
        super(getParentClassLoader());
    }

    public AsmClassLoader(ClassLoader parent){
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
        Class<?> clazz = defineClass(name, b, 0, b.length, DOMAIN);

        return clazz;
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
