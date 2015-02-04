package dbcache.utils.typesafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * 线程安全的Actor
 * <br/>序列执行子类型
 * <br/>需要覆盖run方法, 方法内可以调用super.afterExecute(Object[])执行结束后会进行回调
 * @author Jake
 *
 */
public abstract class SafeActor implements Runnable {
	
	private List<SafeRunable> safeRunables;
	
	private AtomicInteger count = new AtomicInteger(0);
	
	Object[] afterExecuteArgs;

	
	public SafeActor(SafeType... safeTypes) {
		if (safeTypes == null) {
			throw new IllegalArgumentException("safeTypes");
		}
		
		List<SafeRunable> safeRunables = new ArrayList<SafeRunable>(safeTypes.length);
		for (int i = 0; i < safeTypes.length;i++) {
			safeRunables.add(new SafeRunable(safeTypes[i], this));
		}
		this.safeRunables = safeRunables;
	}

	/**
	 * 迭代
	 * @return 是否执行SafeActor
	 */
	boolean roll() {
		return count.incrementAndGet() >= safeRunables.size();
	}

	/**
	 * 之下下一个联合序列任务
	 */
	void runNext() {
		for (SafeRunable safeRunable : safeRunables) {
			safeRunable.runNext();
		}
	}
	
	/**
     * 执行异常回调()
     * TODO
     * @param t Throwable
     */
    public void onException(Throwable t) {}
	
    /**
     * 执行结束的回调(在序列之外)
     * @param args
     */
    public void afterExecute(Object[] args) {
    	this.afterExecuteArgs = args;
    }
    
    
    /**
     * 开始执行Actor
     */
    public void start() {
    	// 执行SafeRunable执行子序列
    	for (SafeRunable safeRunable : safeRunables) {
    		safeRunable.execute();
    	}
    }

}
