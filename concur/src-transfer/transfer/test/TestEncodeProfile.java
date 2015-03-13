package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;

import java.util.Date;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestEncodeProfile {

    public static void main(String[] args) {

        Entity entity = new Entity();
        entity.setId(System.currentTimeMillis());
        entity.setUid(-101);
        entity.setFval(2.34f);
        entity.setStatus(AcountStatus.OPEN);
        entity.setDate(new Date());
        entity.setStr("jake");
        entity.setBool(true);
        entity.getFriends().add(1l);
        entity.getFriends().add(2l);
        entity.getFriends().add(3l);
        
        ByteArray byteArray = Transfer.encode(entity, Entity.class);
        System.out.println("length:" + byteArray.toBytes().length);

        long t1 = System.currentTimeMillis();

        for (int i = 0; i < 10000000;i++) {
            byteArray = Transfer.encode(entity, Entity.class);
        }

        System.out.println(System.currentTimeMillis() - t1);


    }

}
