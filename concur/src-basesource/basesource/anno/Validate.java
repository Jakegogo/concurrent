package basesource.anno;

import basesource.validators.Validator;

/**
 * 自定义验证注解
 * @author Jake
 *
 */
public @interface Validate {
	
	/**
	 * 自定义验证器
	 * @return
	 */
	public Class<? extends Validator> cls();
	
	/**
	 * 参数
	 * @return
	 */
	public String params();
	
}
