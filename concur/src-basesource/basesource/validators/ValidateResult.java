package basesource.validators;

/**
 * 验证结果
 * @author Jake
 *
 */
public class ValidateResult {
	
	/**
	 * 是否验证通过
	 */
	private boolean valid;
	
	/**
	 * 验证失败信息
	 */
	private String msg;
	
	/**
	 * 获取实例
	 * @param valid 是否验证通过
	 * @param msg 验证失败信息(当valid=false)
	 * @return
	 */
	public static ValidateResult valueOf(boolean valid, String msg) {
		ValidateResult result = new ValidateResult();
		result.valid = valid;
		result.msg = msg;
		return result;
	}
	

	public boolean isValid() {
		return valid;
	}


	public String getMsg() {
		return msg;
	}

}
