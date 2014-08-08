package dbcache.test;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Id;
import javax.persistence.Transient;

import dbcache.model.EntityInitializer;

@javax.persistence.Entity
public class Entity implements EntityInitializer {
	
	@Id
	public int id = 1;
	
	
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

}