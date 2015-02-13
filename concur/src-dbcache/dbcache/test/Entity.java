package dbcache.test;

import dbcache.annotation.Cached;
import dbcache.annotation.ChangeFields;
import dbcache.annotation.DynamicUpdate;
import dbcache.annotation.JsonType;
import dbcache.conf.CacheType;
import dbcache.conf.PersistType;
import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;
import org.apache.mina.util.ConcurrentHashSet;
import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

@Cached(persistType=PersistType.INTIME, enableIndex = true, cacheType=CacheType.LRU, entitySize = 1000)
@javax.persistence.Entity
@DynamicUpdate
//@MappedSuperclass
public class Entity implements EntityInitializer, IEntity<Long> {

	public static final String NUM_INDEX = "num_idx";

	@Id
	public Long id;

	private int uid;

	@Index(name=NUM_INDEX)
	public int num;

	private String name;

	public byte[] a = new byte[100];

	@Transient
	private AtomicInteger idgenerator;


	@JsonType
	@Column(columnDefinition="varchar(255) null comment '已经领取过的奖励Id'")
	private ConcurrentHashSet<Long> friends = new ConcurrentHashSet<Long>();

	public Entity() {
//		doAfterLoad();
//		doAfterLoad();
	}

//	@UpdateIndex({ "num_idx" })
//	@ChangeFields({"num"})
	public void increseNum() {
		this.uid = this.idgenerator.incrementAndGet();
	}

	public void addNum() {
		this.increseNum();
	}

	public int getNum() {
		return num;
	}

	private void resetNum() {
		this.idgenerator = new AtomicInteger(num);
		this.uid = 0;
	}

	public void doReset() {
		this.resetNum();
	}

	public void setNum(int num) {
		this.num = num;
	}

//	@ChangeIndexes({ "num_idx" })
//	@ChangeFields({"num"})
	public int addNum(int num) {
		this.num += num;
		return this.num;
	}

	@ChangeFields({"friends"})
	public void combine(Entity other, boolean addMap) {
		this.friends.addAll(other.getFriends());
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

	public ConcurrentHashSet<Long> getFriends() {
		return friends;
	}

	public void setFriends(ConcurrentHashSet<Long> friends) {
		this.friends = friends;
	}

}