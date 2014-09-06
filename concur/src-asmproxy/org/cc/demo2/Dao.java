/**
 * Dao.java 11:58:24 AM Apr 27, 2012
 *
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package org.cc.demo2;
/**
 * <p></p>
 * 
 * @author dixingxing	
 * @date Apr 27, 2012
 */
public class Dao {
	public void insert(Object o) {
		System.out.println("insert : " + o);
	}
	
	public String query() {
		System.out.println("execute (Dao): query");
		return "query result (Dao)";
	}
	public String query2() {
		System.out.println("execute (Dao): query2");
		return "query2 result (Dao)";
	}
	
	public int test() {
		System.out.println("execute (Dao) : test");
		return 0;
	}
	
	protected void testProtected() {
		System.out.println("execute (Dao) : testProtected");
		testPrivate();
	}
	
	private void testPrivate() {
		System.out.println("execute (Dao) : testPrivate");
	}
	
	public Dao[] testArray(String[] s,Object o) {
		System.out.println("execute (Dao) : testArray");
		return null;
	}
}
