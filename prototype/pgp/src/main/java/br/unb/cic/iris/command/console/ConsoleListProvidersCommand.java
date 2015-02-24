package br.unb.cic.iris.command.console;

import br.unb.cic.iris.command.AbstractMailCommand;
import br.unb.cic.iris.mail.EmailProvider;
import br.unb.cic.iris.mail.provider.ProviderManager;

/***
 * added by dConsole
 */
public class ConsoleListProvidersCommand extends AbstractMailCommand {
	static final String COMMAND_NAME = "lp";

	@Override
	public void explain() {
		System.out.printf("(%s) - %s %n", COMMAND_NAME, message("command.list.providers.explain"));
	}

	@Override
	public void handleExecute() {
		for (EmailProvider provider : ProviderManager.instance().getProviders()) {
			System.out.printf("%s - %s%n", provider.getName(), provider.getDescription());
		}
	}

	@Override
	public String getCommandName() {
		return COMMAND_NAME;
	}
}