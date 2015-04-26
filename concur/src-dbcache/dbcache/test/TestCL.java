package dbcache.test;  
  
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;  
  
/* 
 * 每隔500ms运行一次，不断加载class 
 */  
class Multirun implements Runnable {  
    public void run() {  
        try {  
            while (true) {  
                // 每次都创建出一个新的类加载器  
                // class需要放在自己package名字的文件夹下  
                String url = System.getProperty("user.dir") + "/lib";// "/lib/yerasel/GetPI.jar";  
                HowswapCL cl = new HowswapCL(url,  
                        new String[] { "yerasel.GetPI" });  
                Class cls = cl.loadClass("yerasel.GetPI");  
                Object foo = cls.newInstance();  
                // 被调用函数的参数  
                Method m = foo.getClass().getMethod("Output", new Class[] {});  
                m.invoke(foo, new Object[] {});  
                Thread.sleep(500);  
            }  
  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
    }  
}  
  
public class TestCL {  
  
    public static Method initAddMethod() {  
        try {  
            Method add = URLClassLoader.class.getDeclaredMethod("addURL",  
                    new Class[] { URL.class });  
            add.setAccessible(true);  
            return add;  
        } catch (Exception e) {  
            throw new RuntimeException(e);  
        }  
    }  
  
    public static void main(String[] args) {  
  
        // 热部署测试代码  
        Thread t;  
        t = new Thread(new Multirun());  
        t.start(); 
    }  
}  