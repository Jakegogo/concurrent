package utils.collections.concurrent;

import org.apache.mina.util.ConcurrentHashSet;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
* Created by Jake on 2014/11/14.
*/
public class CleanupThread extends Thread
{
    // ConcurrentLRUCaches
    private static ConcurrentHashSet<WeakReference<ConcurrentLRUCache>>
            caches = new ConcurrentHashSet<WeakReference<ConcurrentLRUCache>>();

    // 需要Clean的Cache队列
    private ConcurrentLinkedQueue<ConcurrentLRUCache>
            cleanQueue = new ConcurrentLinkedQueue<ConcurrentLRUCache>();

    // 是否停止线程
    private volatile boolean stop = false;

    // 是否已经start Thread
    private boolean started = false;


    public CleanupThread()
    {
    }

    public CleanupThread(ConcurrentLRUCache c)
    {
        caches.add(new WeakReference<ConcurrentLRUCache>(c));
    }

    public void addConcurrentLRUCache(ConcurrentLRUCache c)
    {
        caches.add(new WeakReference<ConcurrentLRUCache>(c));
    }

    @Override
    public void start() {
        if(!started) {
            synchronized (this)
            {
                if(!started) {
                    super.start();
                    started = true;
                }
            }
        }
    }

    @Override
    public void run()
    {

        while (true)
        {
        	ConcurrentLRUCache c = null;
        	if (cleanQueue.peek() == null) {
	            synchronized (this)
	            {
	                if (stop)
	                {
	                    break;
	                }
	                try
	                {
	                    this.wait();
	                }
	                catch (InterruptedException e)
	                {
	                }
	            }
        	}
            if (stop)
            {
                break;
            }

            Set<ConcurrentLRUCache> cleaningQueue = new HashSet<ConcurrentLRUCache>();
            while ((c = cleanQueue.poll()) != null) {
            	cleaningQueue.add(c);
            }
            
            for(ConcurrentLRUCache c1 : cleaningQueue) {
            	long ct1 = System.currentTimeMillis();
            	int cs1 = c1.size();
                c1.markAndSweep();
				System.out.println("ConcurrentLRUCache回收对象:"
						+ (System.currentTimeMillis() - ct1) + "毫秒,回收前大小:"
						+ cs1 + ",回收后大小:" + c1.size());
            }
            
            
            for(WeakReference<ConcurrentLRUCache> cache : caches) {
                c = cache.get();
                if (c == null) {
                    caches.remove(cache);
                    continue;
                }
            }
        }
    }


    void wakeThread(ConcurrentLRUCache target)
    {
    	boolean isWatting = cleanQueue.peek() == null;
    	cleanQueue.add(target);
    	if (!isWatting) {
            return;
        }
        synchronized (this)
        {
            this.notify();
        }
    }


    void stopThread(ConcurrentLRUCache target)
    {
        synchronized (this)
        {
            for(WeakReference<ConcurrentLRUCache> cache : caches) {

                ConcurrentLRUCache c = cache.get();
                if (c == target) {
                    caches.remove(cache);
                    break;
                }
            }
            cleanQueue.remove(target);
            if(caches.isEmpty()) {
                stop = true;
            }
            this.notify();
        }
    }


}
