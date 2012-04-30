package aeminium.gpu.templates;

import java.io.BufferedReader;

import aeminium.gpu.utils.PathHelper;

public class TemplateWrapper {
	String fname;

	public TemplateWrapper(String file) {
		fname = "templates/" + file;
		
	}

	public BufferedReader getReader() {
		return PathHelper.openFile(fname);
	}

	public String getFileName() {
		return fname;
	}

	public String toString() {
		return fname;
	}

}
