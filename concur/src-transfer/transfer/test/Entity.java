package transfer.test;

import dbcache.EntityInitializer;
import dbcache.IEntity;
import dbcache.anno.ChangeFields;
import dbcache.anno.JsonType;
import org.apache.mina.util.ConcurrentHashSet;
import org.hibernate.annotations.Index;
import transfer.anno.Transferable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

//@MappedSuperclass
@Transferable(id = 1)
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

	private float fval = 1.23f;

	private AcountStatus status;

	private Date date;

	private String str;

	private Boolean bool;


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

	public void combine(Object object, boolean addMap) {
		
	}

	public float getFval() {
		return fval;
	}

	public void setFval(float fval) {
		this.fval = fval;
	}

	public AcountStatus getStatus() {
		return status;
	}

	public void setStatus(AcountStatus status) {
		this.status = status;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public Boolean getBool() {
		return bool;
	}

	public void setBool(Boolean bool) {
		this.bool = bool;
	}
}