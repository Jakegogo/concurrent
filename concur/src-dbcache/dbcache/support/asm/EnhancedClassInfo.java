package dbcache.support.asm;

/**
 * 代理类生成信息
 * Created by Jake on 2015/2/8.
 */
public class EnhancedClassInfo<T> {

    private Class<T> proxyClass;

    private ConstructorBuilder constructorBuilder;


    public Class<T> getProxyClass() {
        return proxyClass;
    }

    public void setProxyClass(Class<T> proxyClass) {
        this.proxyClass = proxyClass;
    }

    public ConstructorBuilder getConstructorBuilder() {
        return constructorBuilder;
    }

    public void setConstructorBuilder(ConstructorBuilder constructorBuilder) {
        this.constructorBuilder = constructorBuilder;
    }
}
