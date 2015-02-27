package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;
import transfer.TypeReference;
import transfer.def.Config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestIteratorMap {

    public static void main(String[] args) {

        Config.registerClass(Entity.class, 1);

        Map<Integer, Entity> map = new HashMap<Integer, Entity>();

        Entity entity = new Entity();
        entity.setUid(101);
        entity.getFriends().add(1l);
        entity.getFriends().add(2l);
        entity.getFriends().add(3l);
        map.put(101, entity);

        Entity entity1 = new Entity();
        entity1.setUid(102);
        entity1.getFriends().add(1l);
        entity1.getFriends().add(2l);
        entity1.getFriends().add(3l);
        map.put(102, entity1);

        Entity entity2 = new Entity();
        entity2.setUid(103);
        entity2.getFriends().add(1l);
        entity2.getFriends().add(2l);
        entity2.getFriends().add(3l);
        map.put(103, entity2);

        long t1 = System.currentTimeMillis();

        ByteArray byteArray = Transfer.encode(map);

        byte[] bytes = byteArray.toBytes();


        Iterator<Map.Entry<Integer, Entity>> it = Transfer.iteratorMap(bytes, new TypeReference<Map<Integer, Entity>>() {
        });

        while (it.hasNext()) {
            Map.Entry<Integer, Entity> entry = it.next();
            System.out.println(entry.getKey() + "-" + entry.getValue() + "-" + entry.getValue().getUid());
        }

        System.out.println(System.currentTimeMillis() - t1);


    }

}
