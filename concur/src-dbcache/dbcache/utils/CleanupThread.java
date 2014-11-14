package dbcache.utils;

import org.apache.mina.util.ConcurrentHashSet;

import java.lang.ref.WeakReference;

/**
* Created by Administrator on 2014/11/14.
*/
public class CleanupThread extends Thread
{
    private static ConcurrentHashSet<WeakReference<ConcurrentLRUCache>>
            caches = new ConcurrentHashSet<WeakReference<ConcurrentLRUCache>>();

    private volatile boolean stop = false;

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


    public void init() {
        if(!started) {
            synchronized (this)
            {
                if(!started) {
                    this.start();
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
            for(WeakReference<ConcurrentLRUCache> cache : caches) {

                ConcurrentLRUCache c = cache.get();
                if (c == null) {
                    caches.remove(c);
                    continue;
                }
                c.markAndSweep();
            }
        }
    }


    void wakeThread()
    {
        synchronized (this)
        {
            this.notify();
        }
    }

    void stopThread()
    {
        synchronized (this)
        {
            stop = true;
            this.notify();
        }
    }


}
