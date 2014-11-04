package dbcache.test;

import java.lang.reflect.Field;

import dbcache.support.asm.AbstractFieldGetter;
import dbcache.support.asm.AsmAccessHelper;
import dbcache.support.asm.ValueGetter;

public class FieldGetterTest<T> {

	public static void main(String[] args) throws Exception {
		Entity entity = new Entity();
		entity.setNum(2);

		long t1 = System.currentTimeMillis();
		ValueGetter<Entity> getNum = AsmAccessHelper.createFieldGetter(Entity.class, Entity.class.getField("num"));
		getNum.setTarget(entity);
		for(int i = 0;i < 100000000;i++) {
			getNum.get();
		}
		System.out.println(System.currentTimeMillis() - t1);
		System.out.println(getNum.getName());


		long t2 = System.currentTimeMillis();
		Field field = Entity.class.getField("num");
		try {
			for(int i = 0;i < 100000000;i++) {
				field.get(entity);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - t2);

	}

	public Object get(T paramT)
	  {
	    return Integer.valueOf(((Entity)paramT).getNum());
	  }

	public String getName() {
		return "num";
	}

}
