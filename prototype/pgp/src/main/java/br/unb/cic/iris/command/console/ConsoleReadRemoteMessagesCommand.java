package br.unb.cic.iris.command.console;

import java.util.List;

import br.unb.cic.iris.command.AbstractMailCommand;
import br.unb.cic.iris.core.SystemFacade;
import br.unb.cic.iris.core.exception.EmailException;
import br.unb.cic.iris.core.model.EmailMessage;

/***
 * added by dConsole
 */
public class ConsoleReadRemoteMessagesCommand extends AbstractMailCommand {
	static final String COMMAND_NAME = "read";

	@Override
	public void explain() {
		System.out.printf("(%s) - %s %n", COMMAND_NAME, message("command.read.explain"));
	}

	@Override
	public void handleExecute() throws EmailException {
		List<EmailMessage> messages = SystemFacade.instance().readRemoteMessages();
		for (EmailMessage msg : messages) {
			showMessage(msg);
		}
	}

	@Override
	public String getCommandName() {
		return COMMAND_NAME;
	}
	
	public void showMessage(EmailMessage msg) {
		System.out.println("---------- MESSAGE BEGIN ----------");
		System.out.println("From: " + msg.getFrom());
		System.out.println("To: " + msg.getTo());
		System.out.println("Date: " + msg.getDate());
		System.out.println("Subject: " + msg.getSubject());
		System.out.println("Message: \n" + msg.getMessage());
		System.out.println("--------------- MESSAGE END ---------------\n");
	}
}