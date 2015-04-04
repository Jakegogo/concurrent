package transfer.test;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import dbcache.EntityInitializer;
import dbcache.IEntity;
import transfer.anno.Transferable;

import javax.persistence.Id;
import java.io.Serializable;

//@MappedSuperclass
@Transferable(id = 1)
public class SimpleEntity implements EntityInitializer, IEntity<Long>, Serializable {

	@Id
	@Protobuf(fieldType = FieldType.INT64, order = 1, required = true)
	public Long id;

	@Protobuf(fieldType = FieldType.INT32, order = 2, required = true)
	private int uid;

	@Protobuf(fieldType = FieldType.INT32, order = 3, required = true)
	public int num;

	@Protobuf(fieldType = FieldType.STRING, order = 4, required = true)
	private String name;

	@Protobuf(fieldType = FieldType.BYTES, order = 5, required = true)
	public byte[] a = new byte[100];

	@Protobuf(fieldType = FieldType.FLOAT, order = 6, required = true)
	private float fval = 1.23f;

	@Protobuf(fieldType = FieldType.STRING, order = 7, required = true)
	private String str;

	@Protobuf(fieldType = FieldType.BOOL, order = 8, required = true)
	private Boolean bool;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getA() {
		return a;
	}

	public void setA(byte[] a) {
		this.a = a;
	}

	public float getFval() {
		return fval;
	}

	public void setFval(float fval) {
		this.fval = fval;
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

	@Override
	public void doAfterLoad() {

	}

	@Override
	public void doBeforePersist() {

	}
}