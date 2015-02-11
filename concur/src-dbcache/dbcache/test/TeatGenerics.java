package dbcache.test;

public class TeatGenerics {

	public static void main(String[] args) {
		try {
			A a = B.class.newInstance();
			a.filter(new C());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}
	
}


interface A <PK> {
	public void filter(PK pk);
}

class B implements A<C> {

	@Override
	public void filter(C pk) {
		pk.print();
	}
	
}

class C {
	public void print() {
		System.out.println("c");
	}
}