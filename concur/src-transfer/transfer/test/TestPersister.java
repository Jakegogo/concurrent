package transfer.test;

import transfer.ByteArray;
import transfer.Persister;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestPersister {

    public static void main(String[] args) throws IOException {

//        Config.registerClass(Entity.class, 1);

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

        ByteArray byteArray = Persister.encode(entity, 187);

        byte[] bytes = byteArray.toBytes();
        System.out.println(bytes);
        System.out.println("persist length:" + bytes.length);


        Entity entity1 = Persister.decode(bytes, Entity.class);
        System.out.println(entity1);
        System.out.println(entity1.getId());
        System.out.println(entity1.getUid());
        System.out.println(entity1.getFriends());
        System.out.println(entity1.getStatus());
        System.out.println(entity1.getDate());
        System.out.println(entity1.getStr());
        System.out.println(entity1.getBool());
        System.out.println(entity1.getFval());



        ByteArrayOutputStream bo=new ByteArrayOutputStream();
        ObjectOutputStream oo=new ObjectOutputStream(bo);
        oo.writeObject(entity);
        System.out.println("Serialize length:" + bo.size());

    }

}
;