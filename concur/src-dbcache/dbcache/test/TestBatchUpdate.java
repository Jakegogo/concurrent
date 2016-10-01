package dbcache.test;

import dbcache.CacheObject;
import dbcache.DbCacheService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import utils.JsonUtils;
import utils.thread.ThreadUtils;
import utils.typesafe.finnal.FinalCommitActor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Jake on 10/1 0001.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@Component
public class TestBatchUpdate {

    @Autowired
    private DbCacheService<Entity, Long> cacheService;

    /**
     * 入库线程池
     */
    private ExecutorService executor =  Executors.newFixedThreadPool(2);


    @org.junit.Test
    public void t23() throws InterruptedException {

        final CountDownLatch ct = new CountDownLatch(10);
        long t1 = System.currentTimeMillis();
        for(long j = 1;j <= 10;j++) {
            final long k = j;
            new Thread() {
                @Override
                public void run() {
                    for(int i = 0;i <= 10000000;i++) {
                        Entity entity = cacheService.get(k);
                        entity.increseNum();

                        cacheService.submitUpdate(entity);
                        if (i % 1000000 == 0) {
                            System.out.println(ThreadUtils.dumpThreadPool("入库线程池", cacheService.getThreadPool()));
                        }
                        ct.countDown();
                    }
                }
            }.start();
        }


        ct.await();

        System.out.println(System.currentTimeMillis() - t1);

        while(true) {
            try {
                System.out.println(ThreadUtils.dumpThreadPool("入库线程池", this.cacheService.getThreadPool()));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }



    @org.junit.Test
    public void t23m() throws InterruptedException {

        final Map<Long, CacheObject> map = new HashMap<Long, CacheObject>();
        for (long i = 1; i <= 10;i++) {
            Entity e = new Entity();
            e.setUid(1);
            e.setId(i);
            e.doAfterLoad();
            map.put(i, new CacheObject<Entity>(e, Entity.class, e, null));
        }

        final CountDownLatch ct = new CountDownLatch(10);
        long t1 = System.currentTimeMillis();
        for(long j = 1;j <= 10;j++) {
            final long k = j;
            new Thread() {
                @Override
                public void run() {
                    for(int i = 0;i <= 10000000;i++) {
                        final CacheObject<Entity> entity = map.get(k);
                        entity.getEntity().increseNum();


                        new FinalCommitActor(entity) {
                            @Override
                            public void run() {
                                System.out.println(JsonUtils.object2JsonString(entity));
                            }
                        }.start(executor);

                        if (i % 1000000 == 0) {
                            System.out.println(ThreadUtils.dumpThreadPool("入库线程池", cacheService.getThreadPool()));
                        }

                    }
                    ct.countDown();
                }
            }.start();
        }


        ct.await();

        System.out.println(System.currentTimeMillis() - t1);

        while(true) {
            try {
                System.out.println(ThreadUtils.dumpThreadPool("入库线程池", this.cacheService.getThreadPool()));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
