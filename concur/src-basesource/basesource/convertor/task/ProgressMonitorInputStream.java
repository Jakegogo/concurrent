package basesource.convertor.task;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 进度监控的输入流
 * Created by Jake on 2015/6/14.
 */
public class ProgressMonitorInputStream extends InputStream implements ProgressMonitorable {

    /** 输入流 */
    FileInputStream fileInputStream;

    /** 总进度值 */
    private int available;

    /** 当前进度值 */
    private int cur;

    /** 精确度Int值 */
    private int accuracyInt;

    /** 最后一次更新的进度 */
    private int step;

    /**
     * 构造方法
     * @param fileInputStream FileInputStream
     * @param accuracy 精确度 0-1之间
     */
    public ProgressMonitorInputStream(FileInputStream fileInputStream, float accuracy) {
        this.fileInputStream = fileInputStream;
        try {
            this.available = fileInputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (accuracy > 1) {
            accuracy = 0.2f;
        }
        accuracyInt = (int) (accuracy * 100);
    }

    @Override
    public int read() throws IOException {
        updateProgress(1);
        return fileInputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readCount = fileInputStream.read(b, off, len);
        updateProgress(readCount);
        return readCount;
    }

    /**
     * 增加进度计数
     * 检查神谕字节数
     * @param readCount
     */
    private void updateProgress(int readCount) {
        if (readCount > 0) {
            this.cur += readCount;
        }
        if (this.cur > this.available) {
            this.available = this.cur + 50;
        }
        if(readCount == -1) {
            this.cur = this.available;
        }

        int step = ((int)((float) this.cur/this.available * 100)) / this.accuracyInt;
        if(step > this.step) {
            this.step = step;
            this.updateProgress((double) this.cur / this.available);
        }

    }

    /**
     * 进度变化回调
     * @param progresss
     */
    public void updateProgress(double progresss) {

    }

    /**
     * 获取进度
     * @return 0 - 1之间
     */
    public double getProgress() {
        return ((double) this.cur) / this.available;
    }


}
