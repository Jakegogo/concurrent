package transfer;

import transfer.exceptions.EOFBytesException;

/**
 * 字节数组
 * <br/> 避免字节数组拷贝,从而减少内存开销
 * Created by Jake on 2015/2/23.
 */
public class ByteArray implements Inputable {

    /**
     * 原始字节数组
     */
    private byte[] byteArr;

    /**
     * 起始位置
     */
    private int startIndex;

    /**
     * 结束位置
     */
    private int endIndex;

    /**
     * 当前读取位置
     */
    private int curIndex;


    /**
     * @param byteArr 原始字节数组
     */
    public ByteArray(byte[] byteArr) {
        this.byteArr = byteArr;
        this.startIndex = 0;
        this.curIndex = this.startIndex;
        this.endIndex = byteArr.length;
    }


    /**
     * @param byteArr 原始字节数组
     * @param startIndex 起始位置 包括
     * @param endIndex 结束位置 不包括
     */
    public ByteArray(byte[] byteArr, int startIndex, int endIndex) {
        this.byteArr = byteArr;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.curIndex = startIndex;
    }


    /**
     * 复制字节数组到新的字节数组
     * @return
     */
    public byte[] toBytes() {
        int length = endIndex - startIndex;
        byte[] bytes = new byte[length];
        System.arraycopy(byteArr, startIndex, bytes, 0, length);
        return bytes;
    }


    @Override
    public byte getByte() {

        if (this.curIndex == this.endIndex) {
            throw new EOFBytesException();
        }

        return this.byteArr[this.curIndex++];
    }


    @Override
    public void getBytes(byte[] bytes) {

        int length = bytes.length;

        if (this.curIndex + length > this.endIndex) {
            throw new EOFBytesException();
        }

        System.arraycopy(byteArr, this.curIndex, bytes, 0, length);
        this.curIndex += length;
    }


    @Override
    public ByteArray getByteArray(int length) {
        return new ByteArray(byteArr, this.curIndex, this.curIndex += length);
    }


    public byte[] getByteArr() {
        return byteArr;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

}
