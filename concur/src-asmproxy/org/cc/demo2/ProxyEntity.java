package org.cc.demo2;


public class ProxyEntity extends Entity {
	
	private Entity entity;
	
	public ProxyEntity() {
		super();
	}
	
	public ProxyEntity(Entity entity) {
		this.entity = entity;
	}
	
	@Override
	public void doAfterLoad() {
	}
	
}
