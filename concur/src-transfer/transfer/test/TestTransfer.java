package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;

import java.util.Date;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestTransfer {

    public static void main(String[] args) {

//        Config.registerClass(Entity.class, 1);

        Entity entity = new Entity();
        entity.setUid(101);
        entity.setFval(2.34f);
        entity.setStatus(AcountStatus.OPEN);
        entity.setDate(new Date());
        entity.setStr("jake");
        entity.setBool(true);
        entity.getFriends().add(1l);
        entity.getFriends().add(2l);
        entity.getFriends().add(3l);

        ByteArray byteArray = Transfer.encode(entity, 187);

        byte[] bytes = byteArray.toBytes();
        System.out.println(bytes);
        System.out.println(bytes.length);


        Entity entity1 = Transfer.decode(bytes, Entity.class);
        System.out.println(entity1);
        System.out.println(entity1.getUid());
        System.out.println(entity1.getFriends());
        System.out.println(entity1.getStatus());
        System.out.println(entity1.getDate());
        System.out.println(entity1.getStr());
        System.out.println(entity1.getBool());

    }

}
