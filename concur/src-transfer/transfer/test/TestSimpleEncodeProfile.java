package transfer.test;

import org.apache.mina.core.buffer.IoBuffer;
import transfer.ByteArray;
import transfer.Outputable;
import transfer.Transfer;
import transfer.def.TransferConfig;
import transfer.serializer.Serializer;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestSimpleEncodeProfile {

    public static void main(String[] args) {

        TransferConfig.registerClass(SimpleEntity.class, 1);

        Serializer serializer = Transfer.encodePreCompile(SimpleEntity.class);

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
        for (int i = 0; i < 10000000;i++) {
            final IoBuffer iob = IoBuffer.allocate(130);
            serializer.serialze(new Outputable() {
                @Override
                public void putByte(byte byte1) {
                    iob.put(byte1);
                }

                @Override
                public void putBytes(byte[] bytes) {
                    iob.put(bytes);
                }

                @Override
                public void putBytes(byte[] bytes, int start, int length) {
                    iob.put(bytes, start, length);
                }
            }, entity, null);
        }
        System.out.println(System.currentTimeMillis() - t1);



    }

}