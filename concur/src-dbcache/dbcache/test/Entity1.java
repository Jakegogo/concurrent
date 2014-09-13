package dbcache.test;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;
import dbcache.service.IndexService;

public class Entity1 extends Entity {

	private Entity obj;

	private IndexService indexService;

	public Entity1(Entity entity, IndexService indexService)
	{
		this.obj = entity;
		this.indexService = indexService;
	}


	  public void setNum(int paramInt)
	  {
	    Integer localInteger1 = Integer.valueOf(this.obj.getNum());
	    this.obj.setNum(paramInt);
	    Integer localInteger2 = Integer.valueOf(this.obj.getNum());
	    indexService.update(this.obj.getClass(), "uid_idx", localInteger1, localInteger2);
	  }

}