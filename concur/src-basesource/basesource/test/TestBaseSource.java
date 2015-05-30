package basesource.test;

import basesource.anno.check.Validate;

public class TestBaseSource {
	
	
	@Validate(cls = IdValidator.class, params = "1,2,3", value = {"1","2"})
	private int id;
	
	static String expr() {
		return "";
	}
	
}
