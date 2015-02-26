package transfer.core;

/**
 * 字节数据信息头
 * Created by Jake on 2015/2/23.
 */
public class ByteDataMeta {

    /**
     * 数据标记
     */
    private byte flag;

    /**
     * 可迭代类型
     */
    private boolean iteratorAble;

    /**
     * 元素大小
     */
    private int componentSize;

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public boolean isIteratorAble() {
        return iteratorAble;
    }

    public void setIteratorAble(boolean iteratorAble) {
        this.iteratorAble = iteratorAble;
    }

    public int getComponentSize() {
        return componentSize;
    }

    public void setComponentSize(int componentSize) {
        this.componentSize = componentSize;
    }


}
