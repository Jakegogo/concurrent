package dbcache.test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import dbcache.annotation.Cached;
import dbcache.annotation.UpdateIndex;
import dbcache.conf.CacheType;
import dbcache.conf.PersistType;
import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;

@Cached(persistType=PersistType.DELAY, enableIndex = true, cacheType=CacheType.LRU)
@javax.persistence.Entity
public class Entity implements EntityInitializer, IEntity<Long> {

	public static final String NUM_INDEX = "num_idx";

	@Id
	public Long id;



	private int uid;

	@Index(name=NUM_INDEX)
	public int num;

	public byte[] a = new byte[100];

	@Transient
	public AtomicInteger idgenerator;

	public Entity() {
//		doAfterLoad();
	}

	@UpdateIndex({ "num_idx" })
	public void increseNum() {
		this.num = this.idgenerator.incrementAndGet();
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	@UpdateIndex({ "num_idx" })
	public void addNum(int num) {
		this.num += num;
	}


	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public void doAfterLoad() {
		idgenerator = new AtomicInteger(num);
	}

	@Override
	public void doBeforePersist() {

	}


	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public byte[] getA() {
		return a;
	}

	public void setA(byte[] a) {
		this.a = a;
	}

	public static void main(String[] args) {

		for(Method method : Entity.class.getDeclaredMethods() ) {
			System.out.println(method);
		}

	}

}