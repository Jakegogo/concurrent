package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;
import transfer.TypeReference;
import transfer.def.TransferConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestIteratorPerform {

    public static void main(String[] args) {

        TransferConfig.registerClass(Entity.class, 1);

        List<Entity> list = new ArrayList<Entity>();

        Entity entity = new Entity();
        entity.setUid(101);
        entity.getFriends().add(1l);
        entity.getFriends().add(2l);
        entity.getFriends().add(3l);
        list.add(entity);

        Entity entity1 = new Entity();
        entity1.setUid(102);
        entity1.getFriends().add(1l);
        entity1.getFriends().add(2l);
        entity1.getFriends().add(3l);
        list.add(entity1);

        Entity entity2 = new Entity();
        entity2.setUid(103);
        entity2.getFriends().add(1l);
        entity2.getFriends().add(2l);
        entity2.getFriends().add(3l);
        list.add(entity2);

        long t1 = System.currentTimeMillis();

        ByteArray byteArray = Transfer.encode(list);

        byte[] bytes = byteArray.toBytes();

        System.out.println(bytes.length);


        for (int i = 0;i < 1000000;i++) {

            Iterator it = Transfer.iterator(bytes, new TypeReference<Collection>() {
            });

            while (it.hasNext()) {
                it.next();
//                System.out.println(((Entity) it.next()).getUid());
            }
        }

        System.out.println(System.currentTimeMillis() - t1);


    }

}
