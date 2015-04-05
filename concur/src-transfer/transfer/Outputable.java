package transfer;

/**
 * 可写的数据通道
 * Created by Jake on 2015/2/22.
 */
public interface Outputable {


    /**
     * 写入字节
     * @param byte1
     */
    public void putByte(byte byte1);


    /**
     * 写入字节数组
     * @param bytes
     */
    public void putBytes(byte[] bytes);


    /**
     * 写入字节数组
     * @param bytes
     */
    public void putBytes(byte[] bytes, int start, int length);

}
