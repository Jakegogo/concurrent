package dbcache.test;

import java.util.concurrent.atomic.AtomicInteger;

import dbcache.utils.concurrent.ForkJoinPool;
import dbcache.utils.concurrent.RecursiveTask;


class Sum extends RecursiveTask<Long> {

	static AtomicInteger taskCount = new AtomicInteger(0);
	
	static AtomicInteger taskNum = new AtomicInteger(0);
	
	int low;
	int high;
	int[] array;
	
	int num;

	Sum(int[] arr, int lo, int hi) {
		array = arr;
		low = lo;
		high = hi;
		num = taskNum.incrementAndGet();
	}

	protected Long compute() {
		if (high - low <= TestForkJoin.SEQUENTIAL_THRESHOLD) {
			long sum = 0;
			for (int i = low; i < high; ++i)
				sum += array[i];
			try {
				System.out.println(taskCount.incrementAndGet() + ":" + num);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return sum;
		} else {
			int mid = low + (high - low) / 2;
			Sum left = new Sum(array, low, mid);
			Sum right = new Sum(array, mid, high);
			left.fork();
	        long rightAns = right.compute();
	        long leftAns = left.join();
	        return leftAns + rightAns;
		}
	}

	static long sumArray(int[] array) {
		return TestForkJoin.fjPool.invoke(new Sum(array, 0, array.length));
	}
}


public class TestForkJoin {
	
	static final int SEQUENTIAL_THRESHOLD = 10;
	
	static final int ARRAY_SIZE = 100;
	
	static ForkJoinPool fjPool = new ForkJoinPool(4);
	
	public static void main(String[] args) {
		System.out.println("cpu core:" + Runtime.getRuntime().availableProcessors());
		
		int[] arr = new int[ARRAY_SIZE];
		for (int i = 0; i < ARRAY_SIZE;i++) {
			arr[i] = i;
		}
		
		System.out.println(Sum.sumArray(arr));
		
	}
	
}
