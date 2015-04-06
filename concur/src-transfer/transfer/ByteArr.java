package transfer;

/**
 * 字节数组段
 */
class ByteArr {

    byte[] byteArray;

    int offset;

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


    protected ByteArr checkBounds(ByteBuffer byteBuffer) {
        if (this.offset == this.length) {
            ByteArr newByteArr = this.expandNext(ByteBuffer.EXPAND_STEP_SIZE);
            byteBuffer.curByteArray = newByteArr;
            return newByteArr;
        }
        return this;
    }

    protected ByteArr checkBounds(int expandLength, ByteBuffer byteBuffer) {
        if (this.offset + expandLength > this.length) {
            if (expandLength < ByteBuffer.EXPAND_STEP_SIZE) {
                expandLength = ByteBuffer.EXPAND_STEP_SIZE;
            }
            ByteArr newByteArr = this.expandNext(expandLength);
            byteBuffer.curByteArray = newByteArr;
            return newByteArr;
        }
        return this;
    }

}
