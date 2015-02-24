package br.unb.cic.iris.reflect;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/***
 * added by dBaseCommand
 */
public class JarFileLoader extends URLClassLoader {
	public JarFileLoader(URL[] urls) {
		super(urls);
	}

	public void addFile(String path) throws MalformedURLException {
		String urlPath = "jar:file://" + path + "!/";
		addURL(new URL(urlPath));
	}
}