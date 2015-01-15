

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* 
 *  实现热部署，自定义ClassLoader，加载的是.class 
 */  
public class HotSwapClassLoader extends ClassLoader {
	
	private Map<String, ClassLoader> dynaclazns = new ConcurrentHashMap<String, ClassLoader>(); // 需要由该类加载器直接加载的类名  
    
    public HotSwapClassLoader() {  
        super(null); // 指定父类加载器为 null    
    }  
    
    
    
    public void loadClassByMe(String className) {
    	try {
			InstanceClassLoader newClassLoader = new InstanceClassLoader();
	        newClassLoader.loadClass(className);  
	        dynaclazns.put(className, newClassLoader);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
    	
    }
    
    
    public void loadClassByMe(String path, String className) {
        loadDirectly(path, className);   
    }
    
  
    private Class loadDirectly(String classFilePath, String className) {  
        Class cls = null;  
        
        File classF = new File(classFilePath);  
        try {  
            cls = instantiateClass(className, new FileInputStream(classF),  
                    classF.length());
        } catch (FileNotFoundException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        return cls;  
    }  
  
    private Class instantiateClass(String name, InputStream fin, long len) {  
        byte[] raw = new byte[(int) len];  
        try {
            fin.read(raw);  
            fin.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        
        InstanceClassLoader newClassLoader = new InstanceClassLoader();
        Class clz = newClassLoader.reDefineClass(name, raw, 0, raw.length);
        dynaclazns.put(name, newClassLoader);
        return clz;
    }
    
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    	return loadClass(name, false);
    }
    
    protected Class loadClass(String name, boolean resolve)  
            throws ClassNotFoundException {  
        Class<?> cls = null;
        if(dynaclazns.containsKey(name)) {
        	ClassLoader cl = dynaclazns.get(name);
        	if(cl instanceof InstanceClassLoader) {
	        	cls = ((InstanceClassLoader)cl).dofindLoadedClass(name);
	        	if(cls != null) {
	        		return cls;
	        	}
        	}
        }
        cls = findLoadedClass(name);
        if (cls == null)  
            cls = getSystemClassLoader().loadClass(name);  
        if (cls == null)
            throw new ClassNotFoundException(name);  
        if (resolve)  
            resolveClass(cls);  
        return cls;  
    }
    
    public URL getResource(String name) {
    	return findResource(name);
	}
    
    
    @SuppressWarnings("static-access")
	protected URL findResource(String name) {
    	return this.getClass().getClassLoader().getSystemResource(name);
    }
    
    /**
     * 查找资源列表[URL查找路径]
     */
    public Enumeration<URL> findResources(String name) throws IOException {
        return this.getClass().getClassLoader().getResources(name);
    }
    
    
    private static class InstanceClassLoader extends ClassLoader  {
    	public Class<?> reDefineClass(String name, byte[] raw, int i, int length) {
    		return super.defineClass(name, raw, i, length);
    	}
    	
    	public Class<?> dofindLoadedClass(String name) {
    		return super.findLoadedClass(name);
    	}
    	
    }

}