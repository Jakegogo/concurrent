package dbcache.test;

public class TestClassName {

	public static void main(String[] args) {
		try {
			System.out.println(Class.forName("int"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
