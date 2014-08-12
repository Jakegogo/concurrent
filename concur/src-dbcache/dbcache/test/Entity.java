package dbcache.test;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;

@javax.persistence.Entity
public class Entity implements EntityInitializer, IEntity<Integer> {
	
	@Id
	public int id = 1;
	
	
	@Index(name="uid_idx")
	private int uid;
	
	
	public volatile int num;
	
	@Transient
	public AtomicInteger idgenerator;

	public Entity() {
		doAfterLoad();
	}
	
	public void increseId() {
		this.num = this.idgenerator.incrementAndGet();
	}
	
	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
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
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

}