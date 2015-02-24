package br.unb.cic.iris.core;

import java.io.File;
import java.util.List;

import br.unb.cic.iris.core.exception.EmailException;
import br.unb.cic.iris.core.exception.EmailUncheckedException;
import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.core.model.IrisFolder;
import br.unb.cic.iris.core.model.Status;
import br.unb.cic.iris.i18n.MessageBundle;
import br.unb.cic.iris.mail.EmailClient;
import br.unb.cic.iris.mail.EmailProvider;
import br.unb.cic.iris.mail.IEmailClient;
import br.unb.cic.iris.mail.pgp.SecurityType;
import br.unb.cic.iris.mail.provider.DefaultProvider;
import br.unb.cic.iris.mail.provider.ProviderManager;

public final class SystemFacade {
	public static final String IRIS_HOME = System.getProperty("user.home")+File.separator+".iris";
	private static final SystemFacade instance = new SystemFacade();
	private IEmailClient client;
	private EmailProvider provider;
	private Status status = Status.NOT_CONNECTED;

	private SystemFacade() {
		Configuration config = new Configuration();
		provider = new DefaultProvider(config.getProperties());
		ProviderManager.instance().addProvider(provider);
		connect(provider);
	}

	public static SystemFacade instance() {
		return instance;
	}

	public void connect(EmailProvider provider) {
		setStatus(Status.NOT_CONNECTED);
		this.provider = provider;
		client = new EmailClient(provider);
		setStatus(Status.CONNECTED);
	}

	public void send(EmailMessage message) throws EmailException {
		send(message, new SecurityType[0]);
	}
	
	//TODO descomentar qdo levar para iris
	public void send(EmailMessage message, SecurityType...types) throws EmailException {
		verifyConnection();
		client.send(message, types);
		//message.setDate(new Date());
		//saveMessage(message, IrisFolder.OUTBOX);
		
	}

	private void saveMessage(EmailMessage message) throws EmailException {
		saveMessage(message, IrisFolder.OUTBOX);
	}

	private void saveMessage(EmailMessage message, String folderName) throws EmailException {
		/*IEmailDAO dao = EmailDAO.instance();
		IrisFolder folder = FolderDAO.instance().findByName(folderName);
		message.setFolder(folder);
		dao.saveMessage(message);*/
	}

	public List<IrisFolder> listRemoteFolders() throws EmailException {
		verifyConnection();
		return client.listFolders();
	}
	
	public List<EmailMessage> readRemoteMessages() throws EmailException{
		return client.getMessages(IrisFolder.INBOX);
	}

	public void downloadMessages(String folder) throws EmailException {
		/*verifyConnection();
		SearchTerm searchTerm = null;
		IEmailDAO dao = EmailDAO.instance();
		Date lastMessageReceived = dao.lastMessageReceived();
		System.out.println("**************************** lastMessageReceived=" + lastMessageReceived);
		if (lastMessageReceived != null) {
			searchTerm = new ReceivedDateTerm(ComparisonTerm.GT, lastMessageReceived);
		}
		List<EmailMessage> messages = client.getMessages(folder, searchTerm);
		for (EmailMessage message : messages) {
			saveMessage(message, folder);
		}*/
	}

	private void verifyConnection() {
		if (!isConnected()) {
			throw new EmailUncheckedException(MessageBundle.message("error.not.connected"));
		}
	}

	public boolean isConnected() {
		return Status.CONNECTED == getStatus();
	}

	private void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public EmailProvider getProvider() {
		return provider;
	}
}