package dbcache.test;

import java.io.*;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.SecureClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import sun.misc.JavaNetAccess;
import sun.misc.Resource;
import sun.misc.SharedSecrets;
import sun.misc.URLClassPath;

/**
 * The class loader used for loading from java.class.path. runs in a restricted
 * security context.
 */
public class MyClassLoader extends URLClassLoader {
	public URLClassPath ucp;
	private Map<String, Class<?>> cache = new HashMap();
	private static final Method defineClassNoVerifyMethod;

	static String[] paths = System.getProperty("java.class.path").split(";");

	static URL[] urls = new URL[paths.length];

	static {
		System.out.println(Arrays.toString(paths));
		System.out.println(System.getProperty("java.class.path"));
		for (int i = 0; i < urls.length; i++) {
			try {

				urls[i] = new URL("file:" + paths[paths.length - 1 - i]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		System.out.println(Arrays.toString(urls));
		SharedSecrets.setJavaNetAccess(new JavaNetAccess() {
			public URLClassPath getURLClassPath(URLClassLoader u) {
				return ((MyClassLoader) u).ucp;
			}
		});
		Method m;
		try {
			m = SecureClassLoader.class.getDeclaredMethod(
					"defineClassNoVerify", new Class[] { String.class,
							ByteBuffer.class, CodeSource.class });
			m.setAccessible(true);
		} catch (NoSuchMethodException nsme) {
			m = null;
		}
		defineClassNoVerifyMethod = m;
	}

	public MyClassLoader(URL[] urls) {
		super(MyClassLoader.urls);
		this.ucp = new URLClassPath(MyClassLoader.urls);
	}

	public MyClassLoader(ClassLoader parent) {
		super(MyClassLoader.urls, parent);
		this.ucp = new URLClassPath(MyClassLoader.urls);
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class c = null;

		if (name.contains("hadoop")) {
			c = (Class) this.cache.get(name);
			if (c == null) {
				c = findClass(name);
				this.cache.put(name, c);
			}
		} else {
			c = loadClass(name, false);
		}
		return c;
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String path = name.replace('.', '/').concat(".class");
		Resource res = this.ucp.getResource(path);
		if (res != null) {
			try {
				return defineClass(name, res, true);
			} catch (IOException e) {
				throw new ClassNotFoundException(name, e);
			}
		}
		throw new ClassNotFoundException(name);
	}

	private Class<?> defineClass(String name, Resource res, boolean verify)
			throws IOException {
		int i = name.lastIndexOf('.');
		URL url = res.getCodeSourceURL();
		if (i != -1) {
			String pkgname = name.substring(0, i);

			Package pkg = getPackage(pkgname);
			Manifest man = res.getManifest();
			if (pkg != null) {
				if (pkg.isSealed()) {
					if (!pkg.isSealed(url)) {
						throw new SecurityException(
								"sealing violation: package " + pkgname
										+ " is sealed");
					}

				} else if ((man != null) && (isSealed(pkgname, man))) {
					throw new SecurityException(
							"sealing violation: can't seal package " + pkgname
									+ ": already loaded");
				}

			} else if (man != null)
				definePackage(pkgname, man, url);
			else {
				definePackage(pkgname, null, null, null, null, null, null, null);
			}

		}

		ByteBuffer bb = res.getByteBuffer();
		byte[] bytes = bb == null ? res.getBytes() : null;

		CodeSigner[] signers = res.getCodeSigners();
		CodeSource cs = new CodeSource(url, signers);

		if (!verify) {
			Object[] args = { name, bb == null ? ByteBuffer.wrap(bytes) : bb,
					cs };
			try {
				return (Class) defineClassNoVerifyMethod.invoke(this, args);
			} catch (IllegalAccessException localIllegalAccessException) {
			} catch (InvocationTargetException ite) {
				Throwable te = ite.getTargetException();
				if ((te instanceof LinkageError))
					throw ((LinkageError) te);
				if ((te instanceof RuntimeException)) {
					throw ((RuntimeException) te);
				}
				throw new RuntimeException("Error defining class " + name, te);
			}

		}
		return defineClass(name, bytes, 0, bytes.length, cs);
	}

	private boolean isSealed(String name, Manifest man) {
		String path = name.replace('.', '/').concat("/");
		Attributes attr = man.getAttributes(path);
		String sealed = null;
		if (attr != null) {
			sealed = attr.getValue(Attributes.Name.SEALED);
		}
		if ((sealed == null) && ((attr = man.getMainAttributes()) != null)) {
			sealed = attr.getValue(Attributes.Name.SEALED);
		}

		return "true".equalsIgnoreCase(sealed);
	}
}
