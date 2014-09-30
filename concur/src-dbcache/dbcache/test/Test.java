package dbcache.test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import javax.annotation.Resource;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dbcache.model.CacheObject;
import dbcache.model.FlushMode;
import dbcache.proxy.asm.AsmFactory;
import dbcache.service.Cache;
import dbcache.service.DbCacheService;
import dbcache.utils.JsonUtils;
import dbcache.utils.ThreadUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@Component
public class Test {

	@Autowired
	private DbCacheService<Entity, Long> cacheService;

	public void setCacheService(DbCacheService<Entity, Long> cacheService) {
		this.cacheService = cacheService;
	}



	@Resource(name = "concurrentWeekHashMapCache")
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
	 * @throws InterruptedException
	 */
	@org.junit.Test
	public void t1() throws InterruptedException {


		for(int i = 0;i < 1000000;i++) {
			Entity entity = this.cacheService.get(1l);
//			entity.increseNum();
//			if(i % 1000000 == 0) {
				entity.addNum(1);
//			}
//				if(i%1000 == 0) {
//			Thread.sleep(100);
//				}

			this.cacheService.submitUpdated2Queue(entity);
			if(i%10000000 == 0) {
				System.out.println(ThreadUtils.dumpThreadPool("入库线程池", this.cacheService.getThreadPool()));
			}
			entity = null;
//			System.gc();
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
		for(int i = 0;i < 100000000;i++) {
			this.cacheService.get(1l);
		}
		System.out.println(System.currentTimeMillis() - t1);
	}


	/**
	 * 测试动态生成静态代理类
	 *
	 *
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NotFoundException
	 * @throws CannotCompileException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@org.junit.Test
	public void t4() throws InstantiationException,
			IllegalAccessException, NotFoundException, CannotCompileException,
			NoSuchMethodException, SecurityException, IllegalArgumentException,
			InvocationTargetException {

		Class<Entity> rsCls = AsmFactory.getEnhancedClass(Entity.class);

		Class<?>[] paramTypes = { Entity.class };
		Entity orign = new Entity();
		Object[] params = { orign };
		Constructor<Entity> con = rsCls.getConstructor(paramTypes);
		Entity entity = con.newInstance(params);

		entity.setNum(2);

		System.out.println(entity.getNum());
		// System.out.println(entity.getId());

		System.out.println(orign.getNum());
	}


	@org.junit.Test
	public void t5() {
		int hash = 32;
		hash ^= (hash >>> 20) ^ (hash >>> 12);
		int h = hash ^ (hash >>> 7) ^ (hash >>> 4);
		System.out.println(h&16);
		System.out.println(Integer.toBinaryString(h));
		System.out.println(Integer.toBinaryString(h&16));
		System.out.println(Integer.toBinaryString(16));

		Map<Integer,String> map = new HashMap<Integer,String>();
		map.put(32, "32");
		String str = map.get(32);
		System.out.println(str);
	}



	@org.junit.Test
	public void t6() {
		int hash = 32;
		System.out.println(Integer.toBinaryString(hash));
	}


	@org.junit.Test
	public void t7() {
		System.out.println("C9".hashCode());
		System.out.println("Aw".hashCode());
	}


	@org.junit.Test
	public void t8() {
		System.out.println(Entity.class.hashCode());
		System.out.println(Entity.class.hashCode() * 31);
	}



	@org.junit.Test
	public void t9() {
//		Entity entity = this.cacheService.get(1l);
//
//		entity.setUid(201);
//
//		List<Entity> list = this.cacheService.listByIndex("uid_idx", 201);
//
//		assert list.size() == 1;
//
//		for(Entity entity1 : list) {
//			System.out.println(JsonUtils.object2JsonString(entity1));
//		}

//		this.cacheService.submitUpdated2Queue(entity);

		Entity entity = null;

		for(int i = 0;i < 5000;i ++) {
			entity = new Entity();
			this.cacheService.submitNew2Queue(entity);
			System.gc();
		}


		System.gc();

		entity = this.cacheService.get(300l);
		System.out.println(JsonUtils.object2JsonString(entity));
	}


}
