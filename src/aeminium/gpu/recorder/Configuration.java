package aeminium.gpu.recorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

	protected static final Properties properties;
	protected static String filename;
	
	static {
		filename = System.getenv("AEMINIUMGPU_CONFIG");
		if ( filename == null ) {
			filename = System.getProperty("user.home") + System.getProperty("file.separator") + ".aeminiumgpurc";
		}
		File file = new File(filename);
		properties = new Properties();
		if ( file.exists()  && file.canRead()) {
			FileReader freader;
			try {
				freader = new FileReader(file);
				properties.load(freader);
				freader.close();
			}  catch (IOException e) {
			} 
		}
	}
	

	public static String get(String key) {
		return properties.getProperty(key);
	}

	public static void set(String key, String value) {
		properties.setProperty(key, value);
		save();
	}
	
	public static void append(String key, String value) {
		String val = get(key);
		if (val == null) {
			val = "";
		} else {
			val = val + ",";
		}
		properties.setProperty(key, val + value);
		save();
	}
	
	private static void save() {
		File file = new File(filename);
		try {
			properties.store(new FileOutputStream(file), "");
		} catch (FileNotFoundException e) {
			System.out.println("Failed to write on " + file);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Failed to write on " + file);
			e.printStackTrace();
		}
	}

}
