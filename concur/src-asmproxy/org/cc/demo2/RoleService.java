/**
 * RoleService.java 12:00:23 PM Apr 27, 2012
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
public class RoleService extends Service{
	@SuppressWarnings("unused")
	private String field1 = "";
	
	public String executeOuter(Integer name) {
		System.out.println("executeOuter call super.query()");
		return query();
	}

	@Override
	public String query() {
		System.out.println("execute (RoleService): query");
		return "query result (RoleService)";
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	} 
	
}
