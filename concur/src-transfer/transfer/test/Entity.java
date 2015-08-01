package transfer.test;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import dbcache.EntityInitializer;
import dbcache.IEntity;
import dbcache.anno.ChangeFields;
import dbcache.anno.JsonType;
import org.hibernate.annotations.Index;
import transfer.anno.Transferable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//@MappedSuperclass
@Transferable(id = 1)
public class Entity implements EntityInitializer, IEntity<Long>, Serializable {

	public static final String NUM_INDEX = "num_idx";

	@Id
	@Protobuf(fieldType = FieldType.INT64, order = 1, required = true)
	public Long id;

	@Protobuf(fieldType = FieldType.INT32, order = 2, required = true)
	private int uid;

	@Index(name=NUM_INDEX)
	@Protobuf(fieldType = FieldType.INT32, order = 3, required = true)
	public int num;

	@Protobuf(fieldType = FieldType.STRING, order = 4, required = true)
	private String name;

	@Protobuf(fieldType = FieldType.BYTES, order = 5, required = true)
	public byte[] a = new byte[100];

	@Transient
	private AtomicInteger idgenerator;

	@Protobuf(fieldType = FieldType.FLOAT, order = 6, required = true)
	private float fval = 1.23f;

	private Date date;

	@Protobuf(fieldType = FieldType.STRING, order = 7, required = true)
	private String str;

	@Protobuf(fieldType = FieldType.BOOL, order = 8, required = true)
	private Boolean bool;
	
	private Map<String,Object> map;

	private Object obj;

	private AcountStatus[] statusHis;

	private AcountStatus status;

	private Object[] objArr;

	private int[] iArr = new int[]{1,2,3};


	@JsonType
	@Column(columnDefinition="varchar(255) null comment '已经领取过的奖励Id'")
	private HashSet<Long> friends = new HashSet<Long>();

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

	public HashSet<Long> getFriends() {
		return friends;
	}

	public void setFriends(HashSet<Long> friends) {
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

	public Map<String,Object> getMap() {
		return map;
	}

	public void setMap(Map<String,Object> map) {
		this.map = map;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public AcountStatus[] getStatusHis() {
		return statusHis;
	}

	public void setStatusHis(AcountStatus[] statusHis) {
		this.statusHis = statusHis;
	}

	public Object[] getObjArr() {
		return objArr;
	}

	public void setObjArr(Object[] objArr) {
		this.objArr = objArr;
	}

	public int[] getIArr() {
		return iArr;
	}

	public void setIArr(int[] iArr) {
		this.iArr = iArr;
	}
}