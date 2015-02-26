package dbcache.test;

import dbcache.EnhancedEntity;
import dbcache.EntityInitializer;
import dbcache.IEntity;
import dbcache.index.DbIndexService;
import org.apache.mina.util.ConcurrentHashSet;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class EnhancedByAsm
  implements EntityInitializer, IEntity<Long>
{
  protected Entity obj;
  protected AtomicIntegerArray changeFields;
  protected DbIndexService handler;

  public IEntity getEntity()
  {
    return this.obj;
  }

  public boolean equals(Object paramObject)
  {
    return this.obj.equals(paramObject.getClass() == EnhancedByAsm.class?((EnhancedByAsm)paramObject).obj:paramObject);
  }

  public String toString()
  {
    return this.obj.toString();
  }

  public int hashCode()
  {
    return this.obj.hashCode();
  }

  public String getName()
  {
    return this.obj.getName();
  }

  
  public void combine(EnhancedByAsm paramObjects)
  {
			this.obj.combine(
					paramObjects instanceof EnhancedEntity ? ((EnhancedByAsm) ((EnhancedEntity) paramObjects)
							.getEntity()) : paramObjects, true);
	}
  
  public void combine(Entity paramEntity, boolean paramBoolean)
  {
    this.changeFields.set(5, 1);
    this.obj.combine(paramEntity, paramBoolean);
  }

  public Long getId()
  {
    return this.obj.getId();
  }

  public void setName(String paramString)
  {
    this.changeFields.set(3, 1);
    this.obj.setName(paramString);
  }

  public void addNum()
  {
    this.changeFields.set(1, 1);
    this.obj.addNum();
  }

  public int addNum(int paramInt)
  {
    this.changeFields.set(2, 1);
    Integer localInteger1 = Integer.valueOf(this.obj.getNum());
    int i = this.obj.addNum(paramInt);
    Integer localInteger2 = Integer.valueOf(this.obj.getNum());
    this.handler.update(this.obj, "num_idx", localInteger1, localInteger2);
    return i;
  }

  public ConcurrentHashSet<Long> getFriends()
  {
    return this.obj.getFriends();
  }

  public void setFriends(ConcurrentHashSet<Long> paramConcurrentHashSet)
  {
    this.changeFields.set(5, 1);
    this.obj.setFriends(paramConcurrentHashSet);
  }

  public void setNum(int paramInt)
  {
    this.changeFields.set(2, 1);
    Integer localInteger1 = Integer.valueOf(this.obj.getNum());
    this.obj.setNum(paramInt);
    Integer localInteger2 = Integer.valueOf(this.obj.getNum());
    this.handler.update(this.obj, "num_idx", localInteger1, localInteger2);
  }

  public int getNum()
  {
    return this.obj.getNum();
  }

  public void setA(byte[] paramArrayOfByte)
  {
    this.changeFields.set(4, 1);
    this.obj.setA(paramArrayOfByte);
  }

  public void setId(Long paramLong)
  {
    this.changeFields.set(0, 1);
    this.obj.setId(paramLong);
  }

  public void doAfterLoad()
  {
    this.obj.doAfterLoad();
  }

  public void increseNum()
  {
    this.changeFields.set(1, 1);
    this.obj.increseNum();
  }

  public void doBeforePersist()
  {
    this.obj.doBeforePersist();
  }

  public void doReset()
  {
    this.changeFields.set(1, 1);
    this.obj.doReset();
  }

  public int getUid()
  {
    return this.obj.getUid();
  }

  public void setUid(int paramInt)
  {
    this.changeFields.set(1, 1);
    this.obj.setUid(paramInt);
  }

  public byte[] getA()
  {
    return this.obj.getA();
  }

  public EnhancedByAsm()
  {
  }

  public EnhancedByAsm(Entity paramEntity, AtomicIntegerArray paramAtomicIntegerArray, DbIndexService paramDbIndexService)
  {
    this.obj = paramEntity;
    this.changeFields = paramAtomicIntegerArray;
    this.handler = paramDbIndexService;
  }
}