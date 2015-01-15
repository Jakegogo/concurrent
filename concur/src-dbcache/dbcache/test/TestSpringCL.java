package dbcache.test;



//-Djava.system.class.loader=dbcache.test.HotSwapClassLoader
public class TestSpringCL {
	
	static HotSwapClassLoader clLoader = new HotSwapClassLoader();
	
	static {
		//将当前的应用加载器设置为线程上下文加载器
		Thread.currentThread().setContextClassLoader(clLoader);
	}
	
	public static void main(String[] args) throws InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		clLoader.loadClassByMe("C:\\Entity.class", "dbcache.test.Entity");
		clLoader.loadClassByMe("dbcache.test.AppRunner");
		
		//将当前的应用加载器设置为线程上下文加载器
		Thread.currentThread().setContextClassLoader(clLoader);
		
		Class<?> entityClz = clLoader.loadClass("dbcache.test.Entity");
		
		Object entity = entityClz.newInstance();
		
		System.out.println(entity.toString());
		
		
		Runnable server = (Runnable) clLoader.loadClass("dbcache.test.AppRunner").newInstance();
		Thread t = new Thread(server, "test");
	    t.setContextClassLoader(clLoader);
		t.start();
		t.setContextClassLoader(clLoader);
		
		
		Thread.sleep(5000);
		
		clLoader.loadClassByMe("C:\\Entity.class", "dbcache.test.Entity");
		System.out.println("reload class");

		t.join();
	}

}


