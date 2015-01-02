package dbcache.test;

import dbcache.support.asm.AsmAccessHelper;
import dbcache.support.asm.ValueSetter;
import org.apache.mina.util.ConcurrentHashSet;

import java.util.Set;

/**
 * Created by Administrator on 2015/1/1.
 */
public class FieldSetterTest {

    public static void main(String[] args) throws Exception {

        Entity entity = new Entity();
        entity.setNum(2);

        ValueSetter<Entity> getNum = AsmAccessHelper.createFieldSetter(Entity.class, Entity.class.getDeclaredField("friendSet"));
//        getNum.setTarget(entity);

        Set<Long> set = new ConcurrentHashSet<Long>();
        set.add(111l);
        getNum.set(entity, set);

        System.out.println(entity.getFriendSet());

    }

}
