package basesource.test;

import basesource.anno.Validate;

public class TestBaseSource {
	
	
	@Validate(cls = IdValidator.class, params = "1,2,3")
	private int id;
	
	
	
}
