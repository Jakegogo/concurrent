package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;
import transfer.def.TransferConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import dbcache.utils.JsonUtils;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestTransfer {

    public static void main(String[] args) {

//        Config.registerClass(Entity.class, 1);
    	
    	TransferConfig.preCompileDeserializer(Entity.class);
    	
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
        entity.setA(null);
        
        List<Integer> obj = new ArrayList<Integer>();
        obj.add(123);
        obj.add(456);
        entity.setObj(obj);

        ByteArray byteArray = Transfer.encode(entity, Entity.class);

        byte[] bytes = byteArray.toBytes();
        System.out.println(bytes);
        System.out.println("length:" + bytes.length);
        showLZ4CompressSize(bytes);
        showDeflaterCompressSize(bytes);
        System.out.println("json length:" + JsonUtils.object2Bytes(entity).length);
        showLZ4CompressSize(JsonUtils.object2Bytes(entity));
        showDeflaterCompressSize(JsonUtils.object2Bytes(entity));

        Entity entity1 = Transfer.decode(bytes, Entity.class);
        System.out.println(entity1);
        System.out.println(entity1.getId());
        System.out.println(entity1.getUid());
        System.out.println(entity1.getFriends());
        System.out.println(entity1.getStatus());
        System.out.println(entity1.getDate());
        System.out.println(entity1.getStr());
        System.out.println(entity1.getBool());
        System.out.println(entity1.getFval());
        System.out.println(entity1.getObj());
    }
    
    
    private static void showLZ4CompressSize(byte[] data) {
    	LZ4Factory factory = LZ4Factory.fastestInstance();

        final int decompressedLength = data.length;

        // compress data
        LZ4Compressor compressor = factory.highCompressor(16);
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
        System.out.println("LZ4 compressedLength:" + compressedLength);
    }
    
    
    private static void showDeflaterCompressSize(byte[] data) {
    	
    	byte[] zipbytes = zip(data, 9);
    	
        System.out.println("Deflater compressedLength:" + zipbytes.length);
    }
    
    
    static final int DEFUALT_BUFFER_SIZE = 1024;
    
    public static byte[] zip(byte[] src, int level) {
		if (level < Deflater.NO_COMPRESSION || level > Deflater.BEST_COMPRESSION) {
			FormattingTuple message = MessageFormatter.format("不合法的压缩等级[{}]", level);
			throw new IllegalArgumentException(message.getMessage());
		}
		ByteArrayOutputStream baos = null;
		try{
			Deflater df = new Deflater(level);
			df.setInput(src);
			df.finish();

			baos = new ByteArrayOutputStream(DEFUALT_BUFFER_SIZE);
			byte[] buff = new byte[DEFUALT_BUFFER_SIZE];
			int whileCount = 0;
			while (!df.finished()) {
				if(whileCount >= 9999999){
					break;
				}
				int count = df.deflate(buff);
				baos.write(buff, 0, count);
				whileCount ++;
			}
			df.end();
		}finally{
			if(baos != null){
				try {
					baos.close();
				} catch (IOException e) {
					// 永远不会执行的
				}
			}
		}
		return baos.toByteArray();
	}
    

}
