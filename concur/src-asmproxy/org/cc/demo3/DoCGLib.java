package org.cc.demo3;

public class DoCGLib {
	public static void main(String[] args) {
		CglibProxy proxy = new CglibProxy();
		// 通过生成子类的方式创建代理类
		SayHello proxyImp = (SayHello) proxy.getProxy(SayHello.class);
		proxyImp.say();
	}
}