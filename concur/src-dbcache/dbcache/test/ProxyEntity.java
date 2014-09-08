package dbcache.test;

import dbcache.support.spring.DefaultMethodAspect;


public class ProxyEntity extends Entity {

	private Entity obj;

	@Override
	public void setNum(int num) {
		Object oldValue = Integer.valueOf(this.obj.getNum());
		this.obj.setNum(num);
		Object newValue = Integer.valueOf(this.obj.getNum());
		DefaultMethodAspect.changeIndex(this.obj, "uid_idx", oldValue, newValue);
	}



}
