package br.unb.cic.iris.command.console;

import java.net.URLClassLoader;
import java.util.Scanner;

import br.unb.cic.iris.command.AbstractMailCommand;
import br.unb.cic.iris.command.MailCommand;
import br.unb.cic.iris.reflect.JarFileLoader;

/***
 * added by dConsole
 */
public class LoadConsoleCommand extends AbstractMailCommand {
	public static final String COMMAND_LOAD = "load";

	@Override
	public void explain() {
		System.out.printf("(%s) - %s %n", COMMAND_LOAD, "Testando carga dinamica de command");
	}

	@Override
	public String getCommandName() {
		return COMMAND_LOAD;
	}

	@Override
	public void handleExecute() {
		showClassPath();
		Scanner sc = new Scanner(System.in);
		System.out.println("Jar file path: ");
		String jarPath = sc.nextLine();
		try {
			URLClassLoader loader = (URLClassLoader) getClass().getClassLoader();
			JarFileLoader l = new JarFileLoader(loader.getURLs());
			l.addFile(jarPath);
			System.setProperty("java.class.path", System.getProperty("java.class.path") + System.getProperty("path.separator") + jarPath);
			showClassPath();
			Class c = l.loadClass("br.unb.cic.iris.teste.ConsoleHelloWorldCommand");
			System.out.println(c.getName());
			ConsoleCommandManager.singleton().addCommand((MailCommand) c.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showClassPath() {
		String classpath = System.getProperty("java.class.path");
		System.out.println("classpath=" + classpath);
	}
}