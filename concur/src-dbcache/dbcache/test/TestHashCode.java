package dbcache.test;

public class TestHashCode {
	
	public static void main(String[] args) {
		System.out.println(System.identityHashCode((long) 1));
		System.out.println(System.identityHashCode((long) 1));
		
		System.out.println(System.identityHashCode(1));
		System.out.println(System.identityHashCode(1));
		
		System.out.println(System.identityHashCode(129));
		System.out.println(System.identityHashCode(129));
		
		System.out.println(Long.valueOf(1).hashCode());
		System.out.println(Long.valueOf(6000000).hashCode());
	}
	
}
