package dbcache.test;

import utils.enhance.asm.AsmAccessHelper;
import utils.enhance.asm.ValueGetter;

import java.lang.reflect.Field;

public class FieldGetterTest<T> {

	public static void main(String[] args) throws Exception {
		Entity entity = new Entity();
		entity.setNum(3);

		// 使用asm
		long t1 = System.currentTimeMillis();
		ValueGetter<Entity> getNum = AsmAccessHelper.createFieldGetter(null, Entity.class, Entity.class.getField("num"));
//		getNum.setTarget(entity);
		for(int i = 0;i < 100000000;i++) {
			getNum.get(entity);
		}

		System.out.println(getNum.get(entity));
		System.out.println(System.currentTimeMillis() - t1);
		System.out.println(getNum.getName());

		// 使用反射
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
	    return ((Entity) paramT).getNum();
	  }

	public String getName() {
		return "num";
	}

}
