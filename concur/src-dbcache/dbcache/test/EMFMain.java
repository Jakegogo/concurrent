package dbcache.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.URL;


/**
 * @author Michael Bauer
 * @version 0.9a
 * 
 */
public class EMFMain {
	private static File emfHome;
	
	private static DynamicURLClassLoader dLoader = new DynamicURLClassLoader();

	/**
	 * Main method. Loads the Spring Context XML file and starts all the
	 * services.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Setup the EMF_HOME
		String f = System.getenv("EMF_HOME");
		if (f == null || f.equals("")) {
			// EMF_HOME was not set, use current user.dir
			f = System.getProperty("user.dir");
		}
		try {
			emfHome = new File(f);
			if (!emfHome.exists())
				throw new Exception("EMF_HOME " + f + " did not exist.");
		} catch (Exception e) {
			System.err.println("Could not determine EMF Home: " + e);
			System.exit(-1);
		}

		loadProperties();

		loadLibraries();
		
		try {
			System.out.println("DynamicURLClassLoader:");
			URL [] urls = dLoader.getURLs();
			for(URL url : urls)
				System.out.println("\t"+url);
			
			
			Runnable server = (Runnable) dLoader.loadClass("com.codechimp.emf.EMFServer").newInstance();
		    Thread t = new Thread(server, "EMFServer");
		    t.setContextClassLoader(dLoader);
			t.start();
			t.join();
		} catch (Exception e) {
			System.err
					.println("An unrecoverable exceptions occured trying to start the EMFServerThread: "
							+ e);
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	/**
	 * Loads the properties tiles.
	 * 
	 */
	private static void loadProperties() {
		try {
			File props = new File(emfHome.getAbsoluteFile()
					+ System.getProperty("file.separator","/") + "conf");
			FileFilter filter = new FileFilter() {
				public boolean accept(File file) {
					String p = file.getAbsolutePath();
					return (p.toLowerCase().endsWith(".properties"));
				}
			};

			File[] listing = props.listFiles(filter);
			if (listing != null)
				for (File f : listing)
					System.getProperties().load(new FileInputStream(f));
		} catch (Exception e) {
			System.out.println("An error has occured loading the properties: "
					+ e);
			System.exit(-1);
		}
	}

	/**
	 * Loads all the libraries in the EMF_HOME/libs directory
	 * 
	 */
	private static void loadLibraries() {
		try {
			File libs = new File(emfHome.getAbsolutePath()
					+ System.getProperty("file.separator","/") + "lib");
			FileFilter filter = new FileFilter() {
				public boolean accept(File file) {
					String p = file.getAbsolutePath();
					return (p.toLowerCase().endsWith(".jar") || p.toLowerCase()
							.endsWith(".zip"));
				}
			};

			File[] listing = libs.listFiles(filter);
			for (File f : listing)
				dLoader.addURL(f.toURL());
		} catch (Exception e) {
			System.err
					.println("An error has occured trying to load the libraries: "
							+ e);
			System.exit(-1);
		}
	}
}