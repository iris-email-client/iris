package br.unb.cic.iris;

import java.util.List;

import br.unb.cic.iris.core.SystemFacade;
import br.unb.cic.iris.core.exception.EmailException;
import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.mail.EmailProvider;
import br.unb.cic.iris.mail.EmailReceiver;

public class MainRead {
	
	public static void main(String[] args) {
		EmailProvider provider = SystemFacade.instance().getProvider();
		String encoding = "UTF-8";
		System.out.println("Account: " + provider.getUsername());
		EmailReceiver receiver = new EmailReceiver(provider, encoding);
		try {
			List<EmailMessage> messages = receiver.getMessages("Inbox", 1);
			System.out.println("\n\n QTDE MENSAGENS: "+messages.size()+"\n");
			for (EmailMessage msg : messages) {
				showMessage(msg);
			}
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	static void showMessage(EmailMessage msg) {
		System.out.println("---------- MESSAGE BEGIN ----------");
		System.out.println("From: " + msg.getFrom());
		System.out.println("To: " + msg.getTo());
		System.out.println("Date: " + msg.getDate());
		System.out.println("Subject: " + msg.getSubject());
		System.out.println("Message: \n" + msg.getMessage());
		System.out.println("--------------- MESSAGE END ---------------\n");
	}
	
}
