package dbcache.test;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;

public class Entity1 extends Entity {
	
	private Entity obj;
	
	public Entity1(Entity entity)
	{
		this.obj = entity;
	}
}