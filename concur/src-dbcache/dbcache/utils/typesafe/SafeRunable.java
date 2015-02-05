package dbcache.utils.typesafe;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的SafeRunable
 * 
 * @author Jake
 *
 */
public class SafeRunable implements Runnable {
	
	AtomicReference<SafeRunable> next = new AtomicReference<SafeRunable>(null);
	
	AtomicBoolean valid = new AtomicBoolean(true);
	
	private SafeType safeType;
	
	private SafeActor safeActor;
	
	protected SafeRunable(SafeType safeType, SafeActor safeActor) {
		this.safeType = safeType;
		this.safeActor = safeActor;
	}
	
	
	@Override
	public void run() {
		safeType.currentThread = Thread.currentThread();// 设置当前执行线程
		AtomicReference<SafeActor> waitActorRef = safeType.waitActor;
		

		if (!waitActorRef.compareAndSet(safeActor, null)) {
			SafeActor waitActor = waitActorRef.get();
			if (waitActor != null) {
				waitActor.runCurrent(safeType);
			}
		}
		
		// 迭代执行SafeActor
		boolean scroll = safeActor.roll();

		if (scroll) {
			try {
//				System.out.println("scroll:" + safeType + ",safeActor:" + safeActor.hashCode());
				safeActor.run();
				
				// afterExecute() TODO
				if (safeActor.afterExecuteArgs != null) {
					safeActor.afterExecute(safeActor.afterExecuteArgs);
				}
				
			} catch (Exception e) {
				safeActor.onException(e);
			}

		} else {
//			System.out.println("not scroll:" + safeType + ",safeActor:" + safeActor.hashCode());
		}

		// 执行联合序列的之后的任务
		if (scroll) {
			safeActor.runNext();
		}

	}

	/**
	 * 执行
	 */
	public void execute() {
		// 已经在执行状态内
		if (safeType.currentThread == Thread.currentThread()) {
			this.run();
			return;
		}
		
		// messages from the same client are handled orderly
		AtomicReference<SafeRunable> lastRef = safeType.head;

		if (lastRef.get() == null && lastRef.compareAndSet(null, this)) { // No previous job
			safeActor.setWait(this.safeType);
			this.valid.set(false);//
			this.run();
		} else {
			// CAS loop
			for (; ; ) {

				SafeRunable last = lastRef.get();

				AtomicReference<SafeRunable> nextRef = last.next;

				SafeRunable next = nextRef.get();
				if (next != null) {
					if (next == last && lastRef.compareAndSet(last, this)) {
						// previous message is handled, order is
						// guaranteed.
						safeActor.setWait(this.safeType);
						this.valid.set(false);//
						this.run();
						return;
					}
				} else if (nextRef.compareAndSet(null, this)) {
					lastRef.compareAndSet(last, this);// fail is OK
					// successfully append to previous task
					return;
				}
			}
		}
	}
	
	
	/**
	 * 执行下一个任务
	 */
	protected void runNext() {
		
		safeType.currentThread = null;// 取消当前执行线程
		
		SafeRunable nextRunable = null;
		while (!next.compareAndSet(null, this)) { // has more job to run
			nextRunable = next.get();
			
			nextRunable.safeActor.setWait(nextRunable.getSafeType());
			
			if (nextRunable.valid.compareAndSet(true, false)) {
				nextRunable.run();
				break;
			}
		}
		
	}


	public SafeType getSafeType() {
		return safeType;
	}


	@Override
	public String toString() {
		return "SafeRunable [next=" + next + ", valid=" + valid + ", safeType="
				+ safeType + ", safeActor=" + safeActor + "]";
	}
	
}
