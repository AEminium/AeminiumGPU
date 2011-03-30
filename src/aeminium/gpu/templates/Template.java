package aeminium.gpu.templates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Template {
	private String source;
	
	public Template(File f) {
		try {
			StringBuilder builder = new StringBuilder();
		    BufferedReader in = new BufferedReader(new FileReader(f.getAbsoluteFile()));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	builder.append(str + "\n");
		    }
		    in.close();
		    source = builder.toString();
		} catch (IOException e) {
		}
		
	}
	
	public Template(String source) {
		this.source = source;
	}
	
	public String apply(HashMap<String, String> mapping) {
		String newSource = source;
		for (String key : mapping.keySet()) {
			if (mapping.get(key) == null) {
				mapping.put(key,"");
			}
			
			newSource = newSource.replace((CharSequence) "{{" + key + "}}", mapping.get(key));
		}
		
		return newSource;
	}
	
}
