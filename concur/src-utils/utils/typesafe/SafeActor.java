package utils.typesafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import utils.collections.concurrent.IdentityHashMap;



/**
 * 线程安全的Actor
 * <br/>序列执行子类型
 * <br/>需要覆盖run方法, 方法内可以调用super.afterExecute(Object[])执行结束后会进行回调
 * @author Jake
 *
 */
public abstract class SafeActor implements Runnable {
	
	private IdentityHashMap<SafeType, SafeRunable> safeRunables;
	
	private List<SafeRunable> safeRunableList;
	
	private AtomicInteger count = new AtomicInteger(0);
	
	Object[] afterExecuteArgs;
	
	private int size;
	
	public SafeActor(SafeType... safeTypes) {
		if (safeTypes == null) {
			throw new IllegalArgumentException("safeTypes");
		}
		
		List<SafeRunable> safeRunableList = new ArrayList<SafeRunable>();
		IdentityHashMap<SafeType, SafeRunable> safeRunables = new IdentityHashMap<SafeType, SafeRunable>(safeTypes.length);
		for (int i = 0; i < safeTypes.length;i++) {
			SafeRunable safeRunable = new SafeRunable(safeTypes[i], this);
			safeRunables.put(safeTypes[i], safeRunable);
			safeRunableList.add(safeRunable);
		}
		this.safeRunables = safeRunables;
		this.safeRunableList = safeRunableList;
		this.size = safeTypes.length;
	}

	/**
	 * 迭代
	 * @return 是否执行SafeActor
	 */
	boolean roll() {
		return count.incrementAndGet() == safeRunables.size();
	}

	/**
	 * 之下下一个联合序列任务
	 */
	void runNext() {
		for (SafeRunable safeRunable : safeRunableList) {
			safeRunable.runNext();
		}
	}
	
	/**
	 * 执行当前的SafeRunable
	 * @param current
	 */
	void runCurrent(SafeType current) {
		SafeRunable safeRunable = safeRunables.get(current);
		safeRunable.run();
	}
	
	/**
	 * 设置等待状态
	 * @param current
	 */
	public void setWait(SafeType current) {
		for (SafeRunable safeRunable : safeRunableList) {
    		if (safeRunable.valid.get() && safeRunable.getSafeType() != current) {
    			safeRunable.getSafeType().waitActor.compareAndSet(null, this);
    		}
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
    	for (SafeRunable safeRunable : safeRunableList) {
    		safeRunable.execute();
    	}
    }

	@Override
	public String toString() {
		return "SafeActor [" + this.hashCode() + "]";
	}
    
    

}
