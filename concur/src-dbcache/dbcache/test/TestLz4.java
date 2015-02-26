package dbcache.test;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;
import net.jpountz.xxhash.StreamingXXHash32;
import net.jpountz.xxhash.XXHashFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2015/2/14.
 */
public class TestLz4 {

    public static void main(String[] args) throws IOException {


        LZ4Factory factory = LZ4Factory.fastestInstance();
        byte[] data = "Compressors and decompressors are interchangeable: it is perfectly correct to compress with the JNI bindings and to decompress with a Java port, or the other way around.Compressors might not generate the same compressed streams on all platforms, especially if CPU endianness differs, but the compressed streams can be safely decompressed by any decompressor implementation on any platform.Compressors and decompressors are interchangeable: it is perfectly correct to compress with the JNI bindings and to decompress with a Java port, or the other way around.Compressors might not generate the same compressed streams on all platforms, especially if CPU endianness differs, but the compressed streams can be safely decompressed by any decompressor implementation on any platform.".getBytes("UTF-8");


        final int decompressedLength = data.length;
        System.out.println("dataLength:" + decompressedLength);

        // compress data
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
        System.out.println("compressedLength:" + compressedLength);

     // decompress data
        // - method 1: when the decompressed length is known
        LZ4FastDecompressor decompressor = factory.fastDecompressor();
        byte[] restored = new byte[decompressedLength];
        int compressedLength2 = decompressor.decompress(compressed, 0, restored, 0, decompressedLength);
        // compressedLength == compressedLength2
        System.out.println("compressedLength2:" + compressedLength2);

        // - method 2: when the compressed length is known (a little slower)
        // the destination buffer needs to be over-sized
        LZ4SafeDecompressor decompressor2 = factory.safeDecompressor();
        int decompressedLength2 = decompressor2.decompress(compressed, 0, compressedLength, restored, 0);
        // decompressedLength == decompressedLength2
        System.out.println("decompressedLength2:" + compressedLength);



        XXHashFactory factory1 = XXHashFactory.fastestInstance();
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        int seed = 0x9747b28c; // used to initialize the hash value, use whatever
        // value you want, but always the same
        StreamingXXHash32 hash32 = factory1.newStreamingHash32(seed);
        byte[] buf = new byte[8]; // for real-world usage, use a larger buffer, like 8192 bytes
        for (;;) {
            int read = in.read(buf);
            if (read == -1) {
                break;
            }
            hash32.update(buf, 0, read);
        }
        int hash = hash32.getValue();

        System.out.println(hash);
    }

}
