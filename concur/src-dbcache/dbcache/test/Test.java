package dbcache.test;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.my9yu.common.db.cache.DbCachedService;

import dbcache.model.CacheObject;
import dbcache.model.FlushMode;
import dbcache.service.Cache;
import dbcache.service.DbCacheService;
import dbcache.utils.ThreadUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@Component
public class Test {
	
	@Autowired
	private DbCacheService cacheService;
	
	@Qualifier("concurrentLinkedHashMapCache")
	@Autowired
	private Cache cache;
	
	@Autowired
	private DbCachedService dbCachedService;
	
	
	public static String getEntityIdKey(Serializable id, Class<?> entityClazz) {
		return new StringBuilder().append(entityClazz.getName())
									.append("_")
									.append(id).toString();
	}
	
	
	/**
	 * 测试用例
	 * 框架:dbCacheNew
	 * CPU:core i7 4700
	 * 内存:8G
	 * 次数:1亿次修改入库
	 * 耗时:40s
	 * 发送sql数量:530条
	 */
	@org.junit.Test
	public void t1() {
		Entity entity = new Entity();
		
		cache.put(getEntityIdKey(1, Entity.class), new CacheObject(entity, 1, Entity.class));
		
		for(int i = 0;i < 100000000;i++) {
			entity.increseId();
			this.cacheService.submitUpdated2Queue(1, Entity.class, FlushMode.DELAY);
			if(i%10000000 == 0) {
				System.out.println(ThreadUtils.dumpThreadPool("入库线程池", this.cacheService.getThreadPool()));
			}
		}
//		System.out.println(entity.num);
		
//		this.cacheService.flushAllEntity();
		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
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
	
	
	
	/**
	 * 测试用例
	 * 框架:dbCacheNew
	 * CPU:core i7 4700
	 * 内存:8G
	 * 次数:1亿次查询缓存
	 * 耗时:19s
	 */
	@org.junit.Test
	public void t3() {
		Entity entity = new Entity();
		long t1 = System.currentTimeMillis();
		cache.put(getEntityIdKey(1, Entity.class), new CacheObject(entity, 1, Entity.class));
		for(int i = 0;i < 100000000;i++) {
			this.cacheService.get(1, Entity.class);
		}
		System.out.println(System.currentTimeMillis() - t1);
	}
	
	
	
	
	/**
	 * 测试用例
	 * 框架:dbCache
	 * CPU:core i7 4700
	 * 内存:8G
	 * 次数:1亿次修改入库
	 * 耗时:
	 * 发送sql数量:较少
	 * 现象:线程池等待队列太多任务堆积，当入库量为1kw时，等待任务数量约等于提交的任务
	 * 分析:当提交同一对象修改数量过多时，无法发挥很好的性能
	 */
	@org.junit.Test
	public void t2() {
		
		Entity entity = this.dbCachedService.get(1, Entity.class);
		
		for(int i = 0;i < 100000000;i++) {
			entity.increseId();
			this.dbCachedService.submitUpdated2Queue(1, Entity.class);
			if(i%1000000 == 0) {
				System.out.println(ThreadUtils.dumpThreadPool("入库线程池", this.dbCachedService.getThreadPool()));
			}
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * 测试用例
	 * 框架:dbCache
	 * CPU:core i7 4700
	 * 内存:8G
	 * 次数:1亿次查询缓存
	 * 耗时:20s
	 */
	@org.junit.Test
	public void t4() {
		long t1 = System.currentTimeMillis();
		for(int i = 0;i < 100000000;i++) {
			this.dbCachedService.get(1, Entity.class);
		}
		System.out.println(System.currentTimeMillis() - t1);
	}
	
	
}
