package dbcache.test;
public class TestPrimitive{
public static void main(String[] args) throws Exception {
		System.out.println((char)65);
        System.out.println(isWrapClass(Long.class));
        System.out.println(isWrapClass(Integer.class));
        System.out.println(isWrapClass(String.class)); 
        System.out.println(isWrapClass(TestCL.class));
        System.out.println(long.class.isPrimitive());
        System.out.println(Long.class.isPrimitive());
        System.out.println(isWrapClass(long.class));
    } 

    public static boolean isWrapClass(Class clz) { 
        try { 
           return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) { 
//        	e.printStackTrace();
            return false; 
        } 
    } 
}