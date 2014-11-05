package dbcache.test;

import dbcache.support.asm.AbstractFieldGetter;
import dbcache.support.asm.ValueGetter;

public abstract class TestClone {

	public static void main(String[] args) {


		new AbstractFieldGetter(){

			@Override
			public Object get() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setTarget(Object object) {
				// TODO Auto-generated method stub

			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}}.clone();

	}

}
