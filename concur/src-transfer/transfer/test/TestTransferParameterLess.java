package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;
import transfer.def.TransferConfig;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestTransferParameterLess {

    public static void main(String[] args) {

        TransferConfig.registerClass(Entity.class, 1);

        Entity entity = new Entity();
        entity.setUid(101);
        entity.getFriends().add(1l);
        entity.getFriends().add(2l);
        entity.getFriends().add(3l);

        ByteArray byteArray = Transfer.encode(entity);

        byte[] bytes = byteArray.toBytes();
        System.out.println(bytes);
        System.out.println(bytes.length);


        Entity entity1 = (Entity) Transfer.decode(bytes, Object.class);
        System.out.println(entity1);
        System.out.println(entity1.getUid());
        System.out.println(entity1.getFriends());
    }

}
