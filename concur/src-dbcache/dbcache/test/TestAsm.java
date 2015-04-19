package dbcache.test;

import utils.enhance.asm.AsmClassLoader;
import utils.enhance.asm.util.AsmUtils;

/**
 * Created by Administrator on 2015/1/3.
 */
public class TestAsm {

    /**
     * 字节码类加载器
     */
    public static final AsmClassLoader classLoader = new AsmClassLoader();

    public static void main(String[] args) throws Exception {

        Entity entity = new Entity();
        //load class
        Class<ValueGetter<Entity>> enhancedClass = (Class<ValueGetter<Entity>>) classLoader.defineClass(
                "SimpleValueGetter", SimpleValueGetterDump.dump ());


        AsmUtils.writeClazz("SimpleValueGetter", SimpleValueGetterDump.dump ());

        enhancedClass.newInstance().set(entity, 101);

        System.out.println(entity.getNum());

    }

}
