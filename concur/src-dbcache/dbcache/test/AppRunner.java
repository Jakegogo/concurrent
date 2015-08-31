package dbcache.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * APP启动器
 * 
 * @author Jake
 *
 */
public class AppRunner implements Runnable {

	@Override
	public void run() {
		
		
//		InputStream in = this.getClass().getClassLoader().getResourceAsStream(
//				"applicationContext.xml");
//
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//
//		String s;
//		try {
//			while ((s = br.readLine()).length() != 0) {
//				System.out.println(s);
//			}
//			br.close();
//		} catch (Exception e) {
//		}
		
		System.out.println(new Entity().toString());
		Object c1 = Entity.class.getClassLoader();
		System.out.println(c1);
		
		Object c2 = this.getClass().getClassLoader();
		System.out.println(c2);
		
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "classpath:applicationContext.xml" });

		System.out.println(applicationContext);
		
		Test test = applicationContext.getBean(Test.class);
		
		System.out.println("first call");
		System.out.println(new Entity().toString());
		test.testChangeIndexValue();
		
		System.out.println(new Entity().toString());
		
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("second call");
		test.testChangeIndexValue();
		
		System.out.println(new Entity().getName());

	}

	public static void main(String[] args) {
		new AppRunner().run();
	}

}
