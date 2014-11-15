package dbcache.utils;

import org.apache.mina.util.ConcurrentHashSet;

import java.lang.ref.WeakReference;

/**
* Created by Administrator on 2014/11/14.
*/
public class CleanupThread extends Thread
{
    // ConcurrentLRUCaches
    private static ConcurrentHashSet<WeakReference<ConcurrentLRUCache>>
            caches = new ConcurrentHashSet<WeakReference<ConcurrentLRUCache>>();

    // 需要Clean的Cache队列
    private ConcurrentHashSet<ConcurrentLRUCache>
            cleanQueue = new ConcurrentHashSet<ConcurrentLRUCache>();

    // 是否停止线程
    private volatile boolean stop = false;

    // 正在markAndSweep
    private volatile boolean sweeping = false;

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
            if (stop)
            {
                break;
            }
            sweeping = true;
            for(WeakReference<ConcurrentLRUCache> cache : caches) {

                ConcurrentLRUCache c = cache.get();
                if (c == null) {
                    caches.remove(cache);
                    continue;
                }
                if(cleanQueue.remove(c)) {
                    c.markAndSweep();
                }
            }
            sweeping = false;
        }
    }


    void wakeThread(ConcurrentLRUCache target)
    {
        if(sweeping) {
            cleanQueue.add(target);
            return;
        }
        synchronized (this)
        {
            cleanQueue.add(target);
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
