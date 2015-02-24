package br.unb.cic.iris.command.manager;

import br.unb.cic.iris.i18n.MessageBundle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.unb.cic.iris.command.CommandListener;
import br.unb.cic.iris.command.ICommandManager;
import br.unb.cic.iris.command.MailCommand;
import br.unb.cic.iris.core.BaseManager;
import br.unb.cic.iris.core.exception.EmailException;

/***
 * added by dBaseCommand
 */
public abstract class BaseCommandManager implements ICommandManager {
	private BaseManager<MailCommand> manager = new BaseManager<MailCommand>();
	private Set<CommandListener> commandListeners = new HashSet<CommandListener>();

	protected abstract void handleAddCommand(MailCommand command);

	public void addCommand(MailCommand command) throws EmailException {
		System.out.println("Adding command: " + command.getCommandName());
		manager.add(command.getCommandName(), command);
		handleAddCommand(command);
		notifyListeners(command);
	}

	public MailCommand getCommand(String commandName) throws EmailException {
		MailCommand command = manager.get(commandName);
		if (command == null) {
			throw new EmailException(MessageBundle.message("error.command.not.found", new String[] { commandName }));
		}
		return command;
	}

	@Override
	public List<MailCommand> listAll() {
		return manager.getAll();
	}

	public void addCommandListener(CommandListener listener) {
		commandListeners.add(listener);
	}

	private void notifyListeners(MailCommand command) {
		for (CommandListener listener : commandListeners) {
			listener.commandAdded(command);
		}
	}

	@Override
	public void reload() throws Exception {
	}
}