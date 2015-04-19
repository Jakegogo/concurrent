package dbcache.test;

import utils.enhance.asm.AsmAccessHelper;
import utils.enhance.asm.ValueSetter;

import org.apache.mina.util.ConcurrentHashSet;

import java.util.Set;

/**
 * Created by Administrator on 2015/1/1.
 */
public class FieldSetterTest {

    public static void main(String[] args) throws Exception {

        Entity entity = new Entity();
        entity.setNum(2);

        ValueSetter<Entity> getNum = AsmAccessHelper.createFieldSetter(Entity.class, Entity.class.getDeclaredField("friends"));
//        getNum.setTarget(entity);

        Set<Long> set = new ConcurrentHashSet<Long>();
        set.add(111l);
        
        long t1 = System.currentTimeMillis();
        for(int i = 0;i < 100000000;i++) {
        	getNum.set(entity, set);
        }
        System.out.println(System.currentTimeMillis() - t1);

        System.out.println(entity.getFriends());

    }

}
