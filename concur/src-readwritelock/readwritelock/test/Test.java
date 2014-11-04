package readwritelock.test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import readwritelock.LockUtils;


public class Test {
	
	@org.junit.Test
	public void test1() {
		
		Entity obj = new Entity();

		ReadWriteLock readWriteLock = LockUtils.getLock(obj);

		Lock readLock = readWriteLock.readLock();

//		Lock writeLock = readWriteLock.writeLock();

		readLock.lock();
		try {
			LockUtils.printStackTrace();
		} finally {
			readLock.unlock();
		}
		
	}
	
	
	class Entity {
		private int i = 0;

		public int getI() {
			return i;
		}

		public void setI(int i) {
			this.i = i;
		}
		
	}

}
