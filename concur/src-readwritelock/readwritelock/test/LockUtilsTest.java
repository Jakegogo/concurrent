package readwritelock.test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import readwritelock.LockUtils;


public class LockUtilsTest {

	public static void main(String[] args) {

		new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				LockUtils.printStackTrace();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				LockUtils.printStackTrace();
			}

		}.start();

		final IdGenerator2[] idGenerators = new IdGenerator2[5];
		for(int i = 0;i < 5;i++) {
			idGenerators[i] = new IdGenerator2();
		}

		int THREADNUM = 1000;
		final CountDownLatch start = new CountDownLatch(1);
		final CountDownLatch end = new CountDownLatch(THREADNUM);

		for (int i = 0; i < THREADNUM; i++) {
			final int no = i;
			new Thread() {

				@Override
				public void run() {
					try {
						start.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					int r = new Random().nextInt(5);
					if (no % 3 == 0) {
						idGenerators[r].increment();
					} else {
						idGenerators[r].getId();
					}
					end.countDown();
				}

			}.start();
		}

		start.countDown();

		try {
			end.await();
			System.out.println(idGenerators[0].getId());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}

class IdGenerator2 {

	private ReadWriteLock lock = LockUtils.getLock(this);

	private Lock readLock = lock.readLock();

	private Lock writeLock = lock.writeLock();

	private int id = 0;

	public IdGenerator2() {
	}

	public int getId() {
		readLock.lock();
		try {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return id;
		} finally {
			readLock.unlock();
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public void increment() {
		writeLock.lock();
		int id = this.getId();
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		id = id + 1;
		this.setId(id);
		writeLock.unlock();
	}

	public void decrement() {
		writeLock.lock();
		this.id--;
		writeLock.unlock();
	}

}