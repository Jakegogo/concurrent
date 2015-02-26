package dbcache;

/**
 * 代理接口
 * <br/>代理类都会实现该接口
 * <br/>可通过 (obj instanceof EntityAsmFactory)判断
 * @see dbcache.support.asm.EntityAsmFactory
 * @author Jake
 */
public interface EnhancedEntity {
	
	/**
	 * 获取实体
	 * @return
	 */
	public IEntity<?> getEntity();
	
}
