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
public class TestIterator {

    public static void main(String[] args) {

        TransferConfig.registerClass(Entity.class, 1);

        List<Entity> list = new ArrayList<Entity>();

        for (int i = 0; i < 1000;i++) {
	        Entity entity = new Entity();
	        entity.setUid(101 + i);
	        entity.getFriends().add(1l);
	        entity.getFriends().add(2l);
	        entity.getFriends().add(3l);
	        list.add(entity);
        }

        long t1 = System.currentTimeMillis();

        ByteArray byteArray = Transfer.encode(list);

        byte[] bytes = byteArray.toBytes();


        Iterator it = Transfer.iterator(bytes, new TypeReference<Collection>() {
        });

        while (it.hasNext()) {
            System.out.println(((Entity)it.next()).getUid());
        }

        System.out.println(System.currentTimeMillis() - t1);


    }

}
