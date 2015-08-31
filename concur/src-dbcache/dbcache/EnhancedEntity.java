package dbcache;

/**
 * 代理接口
 * <br/>ASM字节码增强的实体类都会实现该接口
 * <br/>可通过 (obj instanceof EnhancedEntity)判断
 * @see dbcache.support.asm.EntityAsmFactory
 * @author Jake
 */
public interface EnhancedEntity {
	
	/**
	 * 获取实体
	 * @return
	 */
	IEntity<?> getEntity();

	/**
	 * 获取引用持有者
	 * @return
	 */
	WeakRefHolder getRefHolder();

}
