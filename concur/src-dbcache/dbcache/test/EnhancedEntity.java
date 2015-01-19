package dbcache.test;

import dbcache.model.EntityInitializer;
import dbcache.model.IEntity;
import dbcache.service.DbIndexService;
import org.apache.mina.util.ConcurrentHashSet;

public class EnhancedEntity
  implements EntityInitializer, IEntity<Long>
{
  protected Entity obj;
  protected DbIndexService handler;

  public EnhancedEntity()
  {
  }

  public EnhancedEntity(Entity paramEntity)
  {
    this.obj = paramEntity;
  }

  public EnhancedEntity(Entity paramEntity, DbIndexService paramDbIndexService)
  {
    this.obj = paramEntity;
    this.handler = paramDbIndexService;
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

  public Long getId()
  {
    return this.obj.getId();
  }

  public void setName(String paramString)
  {
    this.obj.setName(paramString);
  }

  public void setUid(int paramInt)
  {
    this.obj.setUid(paramInt);
  }

  public void setNum(int paramInt)
  {
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
    this.obj.setA(paramArrayOfByte);
  }

  public ConcurrentHashSet getFriendSet()
  {
    return this.obj.getFriendSet();
  }

  public void setId(Long paramLong)
  {
    this.obj.setId(paramLong);
  }

  public void doBeforePersist()
  {
    this.obj.doBeforePersist();
  }

  public void doAfterLoad()
  {
    this.obj.doAfterLoad();
  }

  public void setFriendSet(ConcurrentHashSet paramConcurrentHashSet)
  {
    this.obj.setFriendSet(paramConcurrentHashSet);
  }

  public void increseNum()
  {
    this.obj.increseNum();
  }

  public int addNum(int paramInt)
  {
    Integer localInteger1 = Integer.valueOf(this.obj.getNum());
    int i = this.obj.addNum(paramInt);
    Integer localInteger2 = Integer.valueOf(this.obj.getNum());
    this.handler.update(this.obj, "num_idx", localInteger1, localInteger2);
    return i;
  }

  public int getUid()
  {
    return this.obj.getUid();
  }

  public byte[] getA()
  {
    return this.obj.getA();
  }

  public String getFriends()
  {
    return this.obj.getFriends();
  }

  public void setFriends(String paramString)
  {
    this.obj.setFriends(paramString);
  }
}