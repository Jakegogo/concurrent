package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestTransferProfile {

    public static void main(String[] args) {

        Entity entity = new Entity();
        entity.setUid(101);
        entity.setFval(2.34f);
        entity.getFriends().add(1l);
        entity.getFriends().add(2l);
        entity.getFriends().add(3l);

        long t1 = System.currentTimeMillis();

        for (int i = 0; i < 10000000; i++) {

            ByteArray byteArray = Transfer.encode(entity, Entity.class);

            byte[] bytes = byteArray.toBytes();

            Entity entity1 = Transfer.decode(bytes, Entity.class);
            if (entity1 != null) {

            }
        }

        System.out.println(System.currentTimeMillis() - t1);

    }

}
