package dbcache.test;



public class ProxyEntity extends Entity {

	private Entity obj;

	@Override
	public void setNum(int num) {
		Object oldValue = this.obj.getNum();
		this.obj.setNum(num);
		Object newValue = this.obj.getNum();
	}



}
