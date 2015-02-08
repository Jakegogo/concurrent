package dbcache.support.asm;

import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 链式方法切面
 * Created by Jake on 2015/2/7.
 */
public class ChainedMethodProxyAspect extends AbstractAsmMethodProxyAspect {

    private AbstractAsmMethodProxyAspect next;

    private AbstractAsmMethodProxyAspect current;


    public ChainedMethodProxyAspect(List<ChainedMethodProxyAspect> aspectList) {
        if (aspectList == null || aspectList.size() == 0) {
            throw new IllegalArgumentException("aspectList cannot be null.");
        }

        this.current = aspectList.remove(0);
        if (aspectList.size() > 0) {
            this.next = new ChainedMethodProxyAspect(aspectList);
        }
    }

    public ChainedMethodProxyAspect(AbstractAsmMethodProxyAspect... aspects) {
        if (aspects == null || aspects.length == 0) {
            throw new IllegalArgumentException("aspectList cannot be null.");
        }
        List<ChainedMethodProxyAspect> aspectList = new LinkedList<ChainedMethodProxyAspect>((Collection<? extends ChainedMethodProxyAspect>) Arrays.asList(aspects));

        this.current = aspectList.remove(0);
        if (aspectList.size() > 0) {
            this.next = new ChainedMethodProxyAspect(aspectList);
        }
    }

    @Override
    public void initClassMetaInfo(Class<?> clazz, String enhancedClassName) {
        this.current.initClassMetaInfo(clazz, enhancedClassName);
        if (this.next != null) {
            this.next.initClassMetaInfo(clazz, enhancedClassName);
        }
    }

    @Override
    public void doInitClass(ConstructorBuilder constructorBuilder) {
        this.current.doInitClass(constructorBuilder);
        if (this.next != null) {
            this.next.doInitClass(constructorBuilder);
        }
    }

    @Override
    public int doBefore(Class<?> entityClass, MethodVisitor mWriter, Method method, int locals, String name, int acc, String desc) {
        int localMax = this.current.doBefore(entityClass, mWriter, method, locals, name, acc, desc);
        if (this.next != null) {
            localMax = this.next.doBefore(entityClass, mWriter, method, localMax, name, acc, desc);
        }
        return localMax;
    }

    @Override
    public int doAfter(Class<?> entityClass, MethodVisitor mWriter, Method method, int locals, String name, int acc, String desc) {
        int localMax = locals;
        if (this.next != null) {
            localMax = this.next.doAfter(entityClass, mWriter, method, localMax, name, acc, desc);
        }
        return this.current.doAfter(entityClass, mWriter, method, localMax, name, acc, desc);
    }


}
