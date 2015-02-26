package dbcache.test;

import dbcache.EntityInitializer;
import dbcache.IEntity;

public class Entity$EnhancedByCc1
  implements EntityInitializer, IEntity<Long>
{
  protected Entity obj;

  public Entity$EnhancedByCc1()
  {
  }

  public Entity$EnhancedByCc1(Entity paramEntity)
  {
    this.obj = paramEntity;
  }

  public Long getId()
  {
    Long localInteger = this.obj.getId();
    return localInteger;
  }

//  public Comparable getId()
//  {
//    Comparable localComparable = this.obj.getId();
//    return localComparable;
//  }

  public void increseNum()
  {
    this.obj.increseNum();
  }

  public void addNum(int paramInt)
  {
    Integer localInteger1 = Integer.valueOf(this.obj.getNum());
    this.obj.addNum(paramInt);
    Integer localInteger2 = Integer.valueOf(this.obj.getNum());
//    DefaultMethodAspect.changeIndex(this.obj, "uid_idx", localInteger1, localInteger2);
  }

  public void setNum(int paramInt)
  {
    Integer localInteger1 = Integer.valueOf(this.obj.getNum());
    this.obj.setNum(paramInt);
    Integer localInteger2 = Integer.valueOf(this.obj.getNum());
//    DefaultMethodAspect.changeIndex(this.obj, "uid_idx", localInteger1, localInteger2);
  }

  public int getNum()
  {
    int i = this.obj.getNum();
    return i;
  }

  public void doAfterLoad()
  {
    this.obj.doAfterLoad();
  }

  public void doBeforePersist()
  {
    this.obj.doBeforePersist();
  }

  public void setId(Long paramInteger)
  {
    this.obj.setId(paramInteger);
  }

//  public void setId(Comparable paramComparable)
//  {
//    this.obj.setId(paramComparable);
//  }

  public int getUid()
  {
    int i = this.obj.getUid();
    return i;
  }

  public void setUid(int paramInt)
  {
    this.obj.setUid(paramInt);
  }
}