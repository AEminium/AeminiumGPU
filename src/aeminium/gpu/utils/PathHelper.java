package aeminium.gpu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PathHelper {

	public static InputStream openFileAsStream(String fname) {
		// First try inside the Jar
		InputStream is = PathHelper.class.getResourceAsStream("/" + fname);
		if (is != null) {
			return is;
		}

		// Fallback to local file.
		File f = new File(fname);
		if (f.exists()) {
			try {
				return new FileInputStream(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.out.println("Could not load file: " + fname);
		System.exit(1);
		return null;
	}

	public static BufferedReader openFile(String fname) {
		return new BufferedReader(
				new InputStreamReader(openFileAsStream(fname)));
	}
}
