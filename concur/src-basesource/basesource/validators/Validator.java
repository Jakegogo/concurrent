package basesource.validators;

/**
 * 验证器接口
 * @author Jake
 *
 */
public interface Validator {
	
	
	/**
	 * 验证接口
	 * @param object 验证目标值
	 * @param msg 错误提示
	 * @return
	 */
	ValidateResult validate(Object object, String msg);
	
	
}
