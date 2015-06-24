package basesource.test;

import basesource.convertor.utils.ClassScanner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Method;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext-test.xml" })
@Component
public class TestSwapBean1 {

	@Autowired
	TestComponent testComponent;

	@Autowired
	ApplicationContext applicationContext;

	@org.junit.Test
	public void 测试动态加载Class() throws Exception {

		testComponent.printVersion();

		ClassScanner cs = new ClassScanner();
		Set<Class<?>> clzSet = cs.scanPath("I:\\1");


		for (Class<?> clz : clzSet) {
			if (clz.getSimpleName().equals("TestComponent")) {
				Object c1 = applicationContext.getAutowireCapableBeanFactory().createBean(clz);
				Method m = clz.getMethod("printVersion", new Class<?>[]{});
				m.invoke(c1);
			}
		}

		testComponent.printVersion();
		
	}
	
	
}
