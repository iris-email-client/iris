package br.unb.cic.iris;

import br.unb.cic.iris.i18n.MessageBundle;

import java.util.Scanner;

import br.unb.cic.iris.command.console.ConsoleCommandManager;
import br.unb.cic.iris.core.SystemFacade;
/*** added by dConsole
 */
public class MainProgram {
	Scanner sc;
	public MainProgram() {
		sc = new Scanner(System.in);
	}
	public static void main(String args []) {
		MainProgram m = new MainProgram();
		SystemFacade.instance();
		ConsoleCommandManager.singleton().runCommand("");
		m.mainMenu();
		m.readCommand();
	}
	private void mainMenu() {
		System.out.println(MessageBundle.message("interpreter"));
		System.out.println(MessageBundle.message("version"));
		System.out.println(MessageBundle.message("help"));
	}
	private void readCommand() {
		try {
			System.out.print(MessageBundle.message("prompt"));
			String cmd = sc.nextLine().trim();
			ConsoleCommandManager.singleton().runCommand(cmd);
		}
		catch(RuntimeException e) {
			System.err.println(MessageBundle.message("error") +
				e.getLocalizedMessage());
		}
		readCommand();
	}
}