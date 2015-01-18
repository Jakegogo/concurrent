package dbcache.test;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;
import dbcache.service.DbIndexService;

public class Entity1 extends Entity {

	private Entity obj;

	private DbIndexService indexService;

	public Entity1(Entity entity, DbIndexService indexService)
	{
		this.obj = entity;
		this.indexService = indexService;
	}


	  public void setNum(int paramInt)
	  {
	    Integer localInteger1 = Integer.valueOf(this.obj.getNum());
	    this.obj.setNum(paramInt);
	    Integer localInteger2 = Integer.valueOf(this.obj.getNum());
	    indexService.update(this.obj, "uid_idx", localInteger1, localInteger2);
	  }

	  @Override
	  public boolean equals(Object paramObject)
	  {
		  if(paramObject.getClass() == this.getClass()) {
			  return this.obj.equals(((EnhancedEntity)paramObject).obj);
		  }
	    return this.obj.equals(paramObject);
	  }

	  public boolean test(int i) {
		  if(i == 1) {
			  if( i == 2) {
			  return true;
			  }
		  }
		  return false;
	  }

}