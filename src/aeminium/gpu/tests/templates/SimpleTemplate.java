package aeminium.gpu.tests.templates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import junit.framework.TestCase;
import aeminium.gpu.templates.Template;

public class SimpleTemplate extends TestCase {
	

	
	public void testTemplateApplication() {
		Template t = new Template("Hello {{name}}!\n {{end}}");
		HashMap<String, String> mapping = new HashMap<String, String>();
		mapping.put("name", "Joaquim");
		mapping.put("end", "Bye");
		assertEquals("Hello Joaquim!\n Bye",t.apply(mapping));
	}
	
	public void testTwice() {
		Template t = new Template("Hello {{name}}!\n {{end}} {{name}}!");
		HashMap<String, String> mapping = new HashMap<String, String>();
		mapping.put("name", "Joaquim");
		mapping.put("end", "Bye");
		assertEquals("Hello Joaquim!\n Bye Joaquim!",t.apply(mapping));
	}
	
	public void testFromFile() {
		File f = null;
		try {
			f = File.createTempFile("hello", "txt");
			BufferedWriter b = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
			b.write("Hello {{name}}! How are you {{when}}? Bye {{name}}!");
			b.close();
		} catch (IOException e) {
		}
		
		Template t = new Template(f);
		HashMap<String, String> mapping = new HashMap<String, String>();
		mapping.put("name", "Joaquim");
		mapping.put("when", "today");
		assertEquals("Hello Joaquim! How are you today? Bye Joaquim!\n",t.apply(mapping));
	} 
	
}
