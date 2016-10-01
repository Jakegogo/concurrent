package utils.typesafe;

import java.util.concurrent.ExecutorService;

/**
 * <h5>线程安全的Actor</h5>
 * <br/>支持单个SafeType对应的SafeActor操作(run方法)的顺序执行。
 * <p>可并发提交。与提交线程的关系可能同步或异步</p>
 * <br/>需要有序执行的操作可继承此类，并实现run方法
 * @author Jake
 */
public abstract class SafeActor implements Runnable {
	
	protected SafeRunner safeRunner;

	protected SafeActor(){}
	/**
	 * 构造方法
	 * @param safeType 处理需要保证线程安全的数据对象
     */
	public SafeActor(SafeType safeType) {
		if (safeType == null) {
			throw new IllegalArgumentException("safeType cannot be null.");
		}
		this.safeRunner = new SafeRunner(safeType, this);
	}
	
	/**
     * 执行异常回调()
     * @param t Throwable
     */
    public void onException(Throwable t) {
    	t.printStackTrace();
    }

	/**
	 * 开始执行Actor
	 */
	public void start() {
		if (safeRunner == null) {
			throw new IllegalArgumentException("safeRunner cannot be null.");
		}
		// 执行SafeRunable序列
		safeRunner.execute();
	}

	/**
	 * 使用线程池执行Actor
	 * @return
	 */
	public void start(ExecutorService executorService) {
		if (safeRunner == null) {
			throw new IllegalArgumentException("safeRunner cannot be null.");
		}
		this.safeRunner.execute(executorService);
	}

	@Override
	public String toString() {
		return "SafeActor [" + this.hashCode() + "]";
	}

}
