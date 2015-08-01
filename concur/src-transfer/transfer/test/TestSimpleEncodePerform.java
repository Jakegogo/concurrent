package transfer.test;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import transfer.ByteArray;
import transfer.Transfer;
import transfer.def.TransferConfig;
import utils.JsonUtils;
import utils.ProtostuffUtils;

import java.io.IOException;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestSimpleEncodePerform {

    public static void main(String[] args) {

        TransferConfig.registerClass(SimpleEntity.class, 1);

        Transfer.encodePreCompile(SimpleEntity.class);

        SimpleEntity entity = new SimpleEntity();
        entity.setId(1L);
        entity.setName("Jake");
        entity.setStr("str");
        entity.setBool(true);
        entity.setUid(101);

        long t1 = 0l;


        t1 = System.currentTimeMillis();
        ByteArray byteArray = Transfer.encode(entity);
        System.out.println(byteArray.toBytes().length);
        for (int i = 0; i < 50000000;i++) {
            byteArray = Transfer.encode(entity, SimpleEntity.class);
        }
        System.out.println("the transfer : " + (System.currentTimeMillis() - t1));




        Codec<SimpleEntity> simpleTypeCodec = ProtobufProxy
                .create(SimpleEntity.class);

        try {
            byte[] bb0 = simpleTypeCodec.encode(entity);
            System.out.println(bb0.length);

            t1 = System.currentTimeMillis();
            for (int i = 0; i < 50000000;i++) {

                // 序列化
                byte[] bb = simpleTypeCodec.encode(entity);
                // 反序列化
    //            SimpleTypeTest newStt = simpleTypeCodec.decode(bb);
            }
            System.out.println("protobuff : " + (System.currentTimeMillis() - t1));
        } catch (IOException e) {
            e.printStackTrace();
        }


        byte[] bytes0 = ProtostuffUtils.object2Bytes(entity);
        System.out.println(bytes0.length);
        t1 = System.currentTimeMillis();
        for (int i = 0; i < 50000000;i++) {
            byte[] bytes = ProtostuffUtils.object2Bytes(entity);
        }
        System.out.println("protostuff : " + (System.currentTimeMillis() - t1));

//        t1 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000;i++) {
//            JsonUtils.object2Bytes(entity);
//        }
//        System.out.println(System.currentTimeMillis() - t1);
//
//
//
//        t1 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000;i++) {
//            JsonUtils.object2JsonString(entity).getBytes();
//        }
//        System.out.println(System.currentTimeMillis() - t1);







//        t1 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000;i++) {
//        	JacksonUtils.object2JsonString(entity);
//        }
//        System.out.println(System.currentTimeMillis() - t1);





        System.out.println(JsonUtils.object2JsonString(entity).getBytes().length);
        System.out.println(JsonUtils.object2Bytes(entity).length);




    }

}