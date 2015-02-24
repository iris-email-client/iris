package br.unb.cic.iris.command.manager;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import br.unb.cic.iris.command.MailCommand;
import br.unb.cic.iris.core.exception.EmailException;
import br.unb.cic.iris.reflect.ClassFinder;

/***
 * added by dBaseCommand
 */
public abstract class AbstractCommandManager extends BaseCommandManager {
	public AbstractCommandManager() {
		try {
			loadClasspathCommands();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (EmailException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void reload() throws Exception {
		loadClasspathCommands();
	}

	private void loadClasspathCommands() throws ClassNotFoundException, InstantiationException, IllegalAccessException, EmailException {
		System.out.println("Scanning commands ...");
		ClassFinder classFinder = new ClassFinder(MailCommand.class);
		Set classesFound = classFinder.getClasses();
		for (Iterator it = classesFound.iterator(); it.hasNext();) {
			String clazzName = it.next().toString();
			Class c = Class.forName(clazzName);
			if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
				MailCommand command = (MailCommand) c.newInstance();
				addCommand(command);
			}
		}
		System.out.println("Total commands found: " + listAll().size());
	}
}