package dbcache.test;

import java.io.Serializable;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
	
	
}
