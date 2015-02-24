package br.unb.cic.iris.reflect;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/***
 * added by dBaseCommand
 */
public class ClassFinder {
	protected long foundClasses = 0;
	protected Class superClass = null;
	protected String requiredPathSubstring = null;
	protected Set classes = new HashSet(2000);

	public ClassFinder() {
	}

	public ClassFinder(Class superClass) {
		this.superClass = superClass;
	}

	public ClassFinder(Class superClass, String requiredPathSubstring) {
		this.superClass = superClass;
		this.requiredPathSubstring = requiredPathSubstring;
	}

	protected void addClassName(String className) {
		if ((this.requiredPathSubstring == null) || (className.indexOf(this.requiredPathSubstring) >= 0)) {
			if (this.superClass == null) {
				this.classes.add(className);
			} else {
				try {
					Class theClass = Class.forName(className, false, this.getClass().getClassLoader());
					if (this.superClass.isAssignableFrom(theClass)) {
						this.classes.add(className);
					}
				} catch (ClassNotFoundException cnfe) {
				} catch (Throwable t) {
				}
			}
		}
	}

	public Set getClasses() {
		String classpath = System.getProperty("java.class.path");
		String pathSeparator = System.getProperty("path.separator");
		StringTokenizer st = new StringTokenizer(classpath, pathSeparator);
		while (st.hasMoreTokens()) {
			File currentDirectory = new File(st.nextToken());
			processFile(currentDirectory.getAbsolutePath(), "");
		}
		return this.classes;
	}

	private void processFile(String base, String current) {
		File currentDirectory = new File(base + File.separatorChar + current);
		if (isArchive(currentDirectory.getName())) {
			try {
				processZip(new ZipFile(currentDirectory));
			} catch (Exception e) {
			}
			return;
		}
		Set directories = new HashSet();
		File[] children = currentDirectory.listFiles();
		if (children == null || children.length == 0) {
			return;
		}
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (child.isDirectory()) {
				directories.add(children[i]);
			} else {
				if (child.getName().endsWith(".class")) {
					String className = getClassName(current + ((current == "") ? "" : File.separator) + child.getName());
					addClassName(className);
					this.foundClasses++;
				}
			}
		}
		for (Iterator i = directories.iterator(); i.hasNext();) {
			processFile(base, current + ((current == "") ? "" : File.separator) + ((File) i.next()).getName());
		}
	}

	protected boolean isArchive(String name) {
		if ((name.endsWith(".jar") || (name.endsWith(".zip")))) {
			return true;
		} else {
			return false;
		}
	}

	protected String getClassName(String fileName) {
		String newName = fileName.replace(File.separatorChar, '.');
		newName = newName.replace('/', '.');
		return newName.substring(0, fileName.length() - 6);
	}

	protected void processZip(ZipFile file) {
		Enumeration files = file.entries();
		while (files.hasMoreElements()) {
			Object tfile = files.nextElement();
			ZipEntry child = (ZipEntry) tfile;
			if (child.getName().endsWith(".class")) {
				addClassName(getClassName(child.getName()));
				this.foundClasses++;
			}
		}
	}
}