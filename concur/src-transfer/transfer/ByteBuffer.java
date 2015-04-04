package transfer;

/**
 * 可变长度字节缓冲
 * Created by Jake on 2015/2/23.
 */
public class ByteBuffer implements Outputable {

    static final int EXPAND_STEP_SIZE = 256;

    private ByteArr rootByteArray;

    private ByteArr curByteArray;

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
        this.curByteArray.checkBounds(1, this)
                .putByte(byte1);
        offset += 1;
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


    @Override
    public void putByte(byte... bytes) {
        this.curByteArray.checkBounds(bytes.length, this)
                .putBytes(bytes);
        offset += bytes.length;
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


    /**
     * 字节数组段
     */
    static class ByteArr {

        byte[] byteArray;

        private int offset;

        ByteArr next;

        int length;

        public ByteArr(int initLen) {
            this.byteArray = new byte[initLen];
            this.length = initLen;
        }

        public ByteArr expandNext(int length) {
            this.next = new ByteArr(length);
            return this.next;
        }

        public void putByte(byte byte1) {
            this.byteArray[offset++] = byte1;
        }

        public void putBytes(byte[] bytes) {
            System.arraycopy(bytes, 0, this.byteArray, offset, bytes.length);
            offset += bytes.length;
        }

        public void putBytes(byte[] bytes, int start, int len) {
            System.arraycopy(bytes, start, this.byteArray, offset, len);
            offset += len;
        }

        private ByteArr checkBounds(int expandLength, ByteBuffer byteBuffer) {
            if (this.offset + expandLength > this.length) {
                if (expandLength < EXPAND_STEP_SIZE) {
                    expandLength = EXPAND_STEP_SIZE;
                }
                ByteArr newByteArr = this.expandNext(expandLength);
                byteBuffer.curByteArray = newByteArr;
                return newByteArr;
            }
            return this;
        }

    }

}
