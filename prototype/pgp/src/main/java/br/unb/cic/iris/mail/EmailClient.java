package br.unb.cic.iris.mail;

import java.util.List;

import javax.mail.search.SearchTerm;

import br.unb.cic.iris.core.exception.EmailException;
import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.core.model.IrisFolder;
import br.unb.cic.iris.mail.pgp.SecurityType;


public class EmailClient implements IEmailClient {
	public static final String CHARACTER_ENCODING = "UTF-8";
	private final EmailSender sender;
	private final EmailReceiver receiver;

	public EmailClient(EmailProvider provider) {
		this(provider, CHARACTER_ENCODING);
	}

	public EmailClient(EmailProvider provider, String encoding) {
		this(provider, encoding, new EmailSender(provider, encoding));
	}

	public EmailClient(EmailProvider provider, String encoding, EmailSender sender) {
		this.sender = sender;
		receiver = new EmailReceiver(provider, encoding);
	}

	@Override
	public void send(EmailMessage email) throws EmailException {
		System.out.println("send message: " + email);
		sender.send(email);
	}

	@Override
	public List<IrisFolder> listFolders() throws EmailException {
		System.out.println("listing folders ...");
		return receiver.listFolders();
	}

	@Override
	public List<EmailMessage> getMessages(String folder) throws EmailException {
		return getMessages(folder, null);
	}

	@Override
	public List<EmailMessage> getMessages(String folder, SearchTerm searchTerm) throws EmailException {
		return receiver.getMessages(folder, searchTerm);
	}

	@Override
	public List<EmailMessage> getMessages(String folder, int seqnum) throws EmailException {
		return receiver.getMessages(folder, seqnum);
	}

	@Override
	public List<EmailMessage> getMessages(String folder, int begin, int end) throws EmailException {
		return receiver.getMessages(folder, begin, end);
	}

	@Override
	public List<String> validateEmailMessage(EmailMessage message) {
		return sender.validateEmailMessage(message);
	}

	//NOVO
	@Override
	public void send(EmailMessage msg, SecurityType...types) throws EmailException {
		sender.send(msg, types);
	}
}