package dbcache.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

/* 
 *  实现热部署，自定义ClassLoader，加载的是.class 
 */  
class HowswapCL extends ClassLoader {  
  
    private String basedir; // 需要该类加载器直接加载的类文件的基目录  
    private HashSet dynaclazns; // 需要由该类加载器直接加载的类名  
  
    public HowswapCL(String basedir, String[] clazns) {  
        super(null); // 指定父类加载器为 null  
        this.basedir = basedir;  
        dynaclazns = new HashSet();  
        loadClassByMe(clazns);  
    }  
  
    private void loadClassByMe(String[] clazns) {  
        for (int i = 0; i < clazns.length; i++) {  
            loadDirectly(clazns[i]);  
            dynaclazns.add(clazns[i]);  
        }  
    }
  
    private Class loadDirectly(String name) {  
        Class cls = null;  
        StringBuffer sb = new StringBuffer(basedir);  
        String classname = name.replace('.', File.separatorChar) + ".class";  
        sb.append(File.separator + classname);  
        File classF = new File(sb.toString());  
        try {  
            cls = instantiateClass(name, new FileInputStream(classF),  
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
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
  
        return defineClass(name, raw, 0, raw.length);  
    }  
  
    protected Class loadClass(String name, boolean resolve)  
            throws ClassNotFoundException {  
        Class cls = null;  
        cls = findLoadedClass(name);  
        if (!this.dynaclazns.contains(name) && cls == null)  
            cls = getSystemClassLoader().loadClass(name);  
        if (cls == null)  
            throw new ClassNotFoundException(name);  
        if (resolve)  
            resolveClass(cls);  
        return cls;  
    }  
}