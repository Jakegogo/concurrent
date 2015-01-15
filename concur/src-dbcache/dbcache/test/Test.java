package dbcache.test;

import dbcache.support.asm.EntityAsmFactory;
import dbcache.support.jdbc.JdbcSupport;
import dbcache.service.Cache;
import dbcache.service.DbCacheService;
import dbcache.utils.*;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import org.apache.mina.util.ConcurrentHashSet;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@Component
public class Test {

	@Autowired
	private DbCacheService<Entity, Long> cacheService;
	
	@Autowired
	private JdbcSupport jdbcSupport;

	public void setCacheService(DbCacheService<Entity, Long> cacheService) {
		this.cacheService = cacheService;
	}



	@Resource(name = "concurrentWeekHashMapCache")
	private Cache cache;

	// 用户名 - ID 缓存
	private CommonCache<ConcurrentHashSet<Integer>> userNameCache = CacheUtils.cacheBuilder(new CacheQuerier<ConcurrentHashSet<Integer>>() {
		@Override
		public ConcurrentHashSet<Integer> query(Object... keys) {
			DetachedCriteria dc = DetachedCriteria.forClass(Entity.class)
					.add(Restrictions.eq("name", keys[0]))
					.setProjection(Projections.id());
			List list = dc.getExecutableCriteria(HibernateUtil.getSession()).list();
			if (list != null && list.size() > 0) {
				return new ConcurrentHashSet<Integer>(list);
			} else {
				return new ConcurrentHashSet<Integer>();
			}
		}
	}).build();


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


		for(int i = 0;i <= 100000000;i++) {
			Entity entity = this.cacheService.get(1l);
//			entity.increseNum();
//			if(i % 1000000 == 0) {
//				entity.addNum(1);
//			}


//			entity.setNum(i);

			entity.setUid(i);

//			if(i%100 == 0) {
//			Thread.sleep(10);
//			}

			this.cacheService.submitUpdate(entity);
			if(i%10000000 == 0) {
				System.out.println(ThreadUtils.dumpThreadPool("入库线程池", this.cacheService.getThreadPool()));
			}

			if(i%1000000 == 0) {
				Thread.sleep(10);
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

		Class<Entity> rsCls = EntityAsmFactory.getEntityEnhancedClass(Entity.class);

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

		Entity entity = null;

		boolean enter = false;
		for(int i = 0;i < 10000000;i ++) {
			entity = new Entity();
			this.cacheService.submitCreate(entity);
			if(i > 100000) {
				if(!enter) {
					System.out.println("enter");
					enter = true;
				}
				this.cacheService.get(100010000000000l + (i-100000));
				if(i%100 == 0)
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("over");

		entity = this.cacheService.get(300l);
		System.out.println(JsonUtils.object2JsonString(entity));

		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@org.junit.Test
	public void t10() {
		long t1 = System.currentTimeMillis();
		Entity entity = this.cacheService.get(1l);

		entity.setNum(202);
		entity.setA(new byte[100]);

		List<Entity> list = this.cacheService.listByIndex(Entity.NUM_INDEX, 202);

		assert list.size() == 1;

		for(Entity entity1 : list) {
			System.out.println(JsonUtils.object2JsonString(entity1));
		}
		System.out.println(entity.getNum());

		entity.getFriendSet().add(4l);
//		entity.getClass()
		this.cacheService.submitUpdate(entity);
		System.out.println("use time :" + (System.currentTimeMillis() - t1));
	}


	@org.junit.Test
	public void t11() {
		new String().getBytes();
	}


	@org.junit.Test
	public void t12() {
		ConcurrentWeakHashMap map = new ConcurrentWeakHashMap<Entity,Integer>();

		for(int i = 0;i < 100000;i++) {
			Entity entity = new Entity();
			entity.setId(Long.valueOf(i));

			map.putIfAbsent(entity, i);

			Assert.assertEquals(entity, Integer.valueOf(i));
		}

	}


	@org.junit.Test
	public void t13() {

//		for(int i = 0;i < 100;i++) {
			Long id = Long.valueOf(100);
			Entity entity = new Entity();
			entity.setId(id);

			this.cacheService.submitCreate(entity);

			Assert.assertEquals(entity, this.cacheService.get(id));
//		}

	}


	@org.junit.Test
	public void t14() {

		for(int i = 101;i < 200;i++) {
			Long id = Long.valueOf(i + 100);
			Entity entity = new Entity();
			entity.setId(id);

			this.cacheService.submitCreate(entity);

//			Assert.assertEquals(entity, this.cacheService.get(id));
		}

		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


//	public void t15() {
//		CyclicBarrier cb = new CyclicBarrier()
//	}


	@org.junit.Test
	public void t16() {
		System.out.println("16 >>> 1 : " + (16 >>> 1));
	}



	@org.junit.Test
	public void t17() {
		System.out.println(userNameCache.get("test"));
	}

	
	@org.junit.Test
	public void t18() {
		List<List> list = jdbcSupport.listBySql(List.class, "select * from entity");
		System.out.println(list);
	}
	

}
