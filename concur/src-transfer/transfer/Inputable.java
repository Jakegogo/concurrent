package transfer;

/**
 * 可读的数据源
 * Created by Jake on 2015/2/22.
 */
public interface Inputable {

    /**
     * 获取下一个字节
     * @return
     */
    byte getByte();


    /**
     * 获取字节到数组,lenth为bytes的长度
     * @param bytes
     */
    void getBytes(byte[] bytes);


    /**
     * 生成ByteArray包装
     * @param length
     * @return
     */
    ByteArray getByteArray(int length);

}
