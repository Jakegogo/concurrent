package dbcache.test;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.HashMap;

public class DynamicURLClassLoader extends URLClassLoader {
	public DynamicURLClassLoader() {
		super(new URL[0]);
	}
	
	public DynamicURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	public DynamicURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public DynamicURLClassLoader(URL[] urls) {
		super(urls);
	}

	@Override
	public void addURL(URL arg0) {
		super.addURL(arg0);
	}
	
}