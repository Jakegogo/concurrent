package transfer.test;

import java.util.Date;

import utils.JsonUtils;
import transfer.ByteArray;
import transfer.Transfer;
import transfer.def.TransferConfig;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestDecodePerform {

    public static void main(String[] args) {

        TransferConfig.registerClass(Entity.class, 1);

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

        ByteArray byteArray = Transfer.encode(entity);

        byte[] bytes = byteArray.toBytes();

        long t1 = 0l;


        String jsonString = JsonUtils.object2JsonString(entity);
        t1 = System.currentTimeMillis();
        for (int i = 0; i < 5000000;i++) {
            Entity entity1 = JsonUtils.jsonString2Object(jsonString, Entity.class);
        }
        System.out.println(System.currentTimeMillis() - t1);

        
        byte[] bytes1 = JacksonUtils.object2Bytes(entity);
        t1 = System.currentTimeMillis();
        for (int i = 0; i < 5000000;i++) {
            Entity entity1 = JacksonUtils.bytes2Object(bytes1, new org.codehaus.jackson.type.TypeReference<Entity>(){});
        }
        System.out.println(System.currentTimeMillis() - t1);
        

        t1 = System.currentTimeMillis();
        for (int i = 0; i < 5000000;i++) {
            Entity entity1 = Transfer.decode(bytes, Entity.class);
        }
        System.out.println(System.currentTimeMillis() - t1);





    }

}