package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;
import transfer.def.Config;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestEncodeProfile {

    public static void main(String[] args) {

        Config.registerClass(Entity.class, 1);

        Entity entity = new Entity();
        entity.setUid(101);
        entity.getFriends().add(1l);
        entity.getFriends().add(2l);
        entity.getFriends().add(3l);

        long t1 = System.currentTimeMillis();

        for (int i = 0; i < 10000000;i++) {
            ByteArray byteArray = Transfer.encode(entity);
        }

        System.out.println(System.currentTimeMillis() - t1);


    }

}
