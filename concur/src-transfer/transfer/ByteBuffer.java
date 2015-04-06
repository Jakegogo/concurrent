package transfer;

/**
 * 可变长度字节缓冲
 * Created by Jake on 2015/2/23.
 */
public class ByteBuffer implements Outputable {

    static final int EXPAND_STEP_SIZE = 256;

    protected ByteArr rootByteArray;

    protected ByteArr curByteArray;

    private int offset;

    public ByteBuffer() {
        this(EXPAND_STEP_SIZE);
    }

    public ByteBuffer(int initLen) {
        this.rootByteArray = new ByteArr(initLen);
        this.curByteArray = this.rootByteArray;
    }

    @Override
    public void putByte(byte byte1) {
        this.curByteArray.checkBounds(this)
                .putByte(byte1);
        offset ++;
    }


    @Override
    public void putBytes(byte[] bytes) {
        this.curByteArray.checkBounds(bytes.length, this)
                .putBytes(bytes);
        offset += bytes.length;
    }

    @Override
    public void putBytes(byte[] bytes, int start, int length) {
        this.curByteArray.checkBounds(length, this)
                .putBytes(bytes, start, length);
        offset += length;
    }


    /**
     * 获取字节
     * @return
     */
    public ByteArray getByteArray() {

        if (rootByteArray == curByteArray) {
            return new ByteArray(rootByteArray.byteArray, 0, offset);
        }

        byte[] byteArray = new byte[offset];

        ByteArr curBytesArr = this.rootByteArray;
        int loopOffset = 0;
        do {
            System.arraycopy(curBytesArr.byteArray, 0, byteArray, loopOffset, curBytesArr.offset);
            loopOffset += curBytesArr.offset;
        } while ((curBytesArr = curBytesArr.next) != null);

        return new ByteArray(byteArray, 0, offset);
    }


    /**
     * 获取字节数组
     * @return
     */
    public byte[] toBytes() {

        byte[] byteArray = new byte[offset];

        if (rootByteArray == curByteArray) {
            System.arraycopy(rootByteArray.byteArray, 0, byteArray, 0, offset);
            return byteArray;
        }

        ByteArr curBytesArr = this.rootByteArray;
        int loopOffset = 0;
        do {
            System.arraycopy(curBytesArr.byteArray, 0, byteArray, loopOffset, curBytesArr.offset);
            loopOffset += curBytesArr.offset;
        } while ((curBytesArr = curBytesArr.next) != null);

        return byteArray;
    }


    /**
     * 长度
     * @return
     */
    public int length() {
        return this.offset;
    }


}
