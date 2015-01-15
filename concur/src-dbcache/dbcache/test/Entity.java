package dbcache.test;

import dbcache.annotation.Cached;
import dbcache.annotation.JsonConvert;
import dbcache.annotation.UpdateIndex;
import dbcache.conf.CacheType;
import dbcache.conf.PersistType;
import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;

import org.apache.mina.util.ConcurrentHashSet;
import org.hibernate.annotations.Index;

import javax.persistence.Id;
import javax.persistence.Transient;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

@Cached(persistType=PersistType.INTIME, enableIndex = true, cacheType=CacheType.LRU, entitySize = 2)
@javax.persistence.Entity
public class Entity implements EntityInitializer, IEntity<Long> {

	public static final String NUM_INDEX = "num_idx";

	@Id
	public Long id;



	private int uid;

	@Index(name=NUM_INDEX)
	public int num;

	private String name;

	private String friends = "[1,2,3]";

	public byte[] a = new byte[100];

	@Transient
	private AtomicInteger idgenerator;

	public void setFriendSet(ConcurrentHashSet<Long> friendSet) {
		this.friendSet = friendSet;
	}

	@Transient
	@JsonConvert("friends")
	private ConcurrentHashSet<Long> friendSet = new ConcurrentHashSet<Long>();

	public Entity() {
//		doAfterLoad();
//		doAfterLoad();
	}

//	@UpdateIndex({ "num_idx" })
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
	public int addNum(int num) {
		this.num += num;
		return this.num;
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


	@Override
	public int hashCode() {
		return 305668771 + 1793910479 * this.getId().hashCode();
	}
	
	

	@Override
	public String toString() {
		return "测试";
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof Entity)) {
			return false;
		}
		Entity target = (Entity) obj;
		return this.id.equals(target.id);
	}

	public static void main(String[] args) {

		for(Method method : Entity.class.getDeclaredMethods() ) {
			System.out.println(method);
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFriends() {
		return friends;
	}

	public void setFriends(String friends) {
		this.friends = friends;
	}

	public ConcurrentHashSet<Long> getFriendSet() {
		return friendSet;
	}
}