package br.unb.cic.iris.mail;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Store;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;

import br.unb.cic.iris.core.exception.EmailException;
import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.core.model.IrisFolder;
import br.unb.cic.iris.i18n.MessageBundle;
import br.unb.cic.iris.mail.pgp.PgpManager;


/***
 * added by dBaseMail
 */
public class EmailReceiver implements StoreListener, FolderListener {
	private Store store;
	private EmailSession session;
	private EmailProvider provider;
	private String from;

	public EmailReceiver(EmailProvider provider, String encoding) {
		this.provider = provider;
		session = new EmailSession(provider, encoding);
	}

	public List<IrisFolder> listFolders() throws EmailException {
		List<IrisFolder> folders = new ArrayList<IrisFolder>();
		try {
			Store store = getStore();
			Folder defaultFolder = store.getDefaultFolder();
			Folder[] externalFolders = defaultFolder.list();
			for (Folder f : externalFolders) {
				folders.add(new IrisFolder(f.getName()));
			}
		} catch (MessagingException e) {
			throw new EmailException(MessageBundle.message("error.list.folder"), e);
		}
		return folders;
	}

	public List<EmailMessage> getMessages(String folderName, SearchTerm searchTerm) throws EmailException {
		System.out.println("getMessages .....");
		List<EmailMessage> messages = new ArrayList<EmailMessage>();
		Folder folder = openFolder(folderName);
		try {
			Message messagesRetrieved[] = null;
			if (searchTerm == null) {
				messagesRetrieved = folder.getMessages();
			} else {
				messagesRetrieved = folder.search(searchTerm);
			}
			messages = convertToIrisMessage(messagesRetrieved);
		} catch (MessagingException e) {
			throw new EmailException(e.getMessage(), e);
		}
		return messages;
	}

	public List<EmailMessage> getMessages(String folderName, int begin, int end) throws EmailException {
		List<EmailMessage> messages = new ArrayList<EmailMessage>();
		Folder folder = openFolder(folderName);
		try {
			Message messagesRetrieved[] = folder.getMessages(begin, end);
			messages = convertToIrisMessage(messagesRetrieved);
		} catch (MessagingException e) {
			throw new EmailException(e.getMessage(), e);
		}
		return messages;
	}

	public List<EmailMessage> getMessages(String folderName, int seqnum) throws EmailException {
		List<EmailMessage> messages = new ArrayList<EmailMessage>();
		Folder folder = openFolder(folderName);
		try {
			List<Message> messagesList = new ArrayList<Message>();
			int messageCount = folder.getMessageCount();
			for (int i = seqnum; i <= messageCount; i++) {
				Message message = folder.getMessage(i);
				messagesList.add(message);
			}
			Message[] messagesRetrieved = toArray(messagesList);
			messages = convertToIrisMessage(messagesRetrieved);
		} catch (MessagingException e) {
			throw new EmailException(e.getMessage(), e);
		}
		return messages;
	}

	private Message[] toArray(List<Message> messagesList) {
		return messagesList.toArray(new Message[messagesList.size()]);
	}

	private List<EmailMessage> convertToIrisMessage(Message[] messagesRetrieved) throws EmailException {
		List<EmailMessage> messages = new ArrayList<EmailMessage>();
		int cont = 0;
		int total = messagesRetrieved.length;
		for (Message m : messagesRetrieved) {
			try {
				messages.add(convertToIrisMessage(m));
				if (total != 0) {
					for (int i = 0; i < 15; i++) {
						System.out.print('\b');
					}
					cont++;
					int tmp = 100 * cont;
					System.out.print((tmp / total) + "% completed");
				}
			} catch (IOException e) {
				throw new EmailException(e.getMessage(), e);
			} catch (MessagingException e) {
				throw new EmailException(e.getMessage(), e);
			}
		}
		System.out.println();
		return messages;
	}

	private Folder openFolder(String folderName) throws EmailException {
		return openFolder(folderName, Folder.READ_ONLY);
	}

	private Folder openFolder(String folderName, int openType) throws EmailException {
		try {
			Folder folder = getStore().getFolder(folderName);
			folder.open(openType);
			return folder;
		} catch (MessagingException e) {
			throw new EmailException(e.getMessage(), e);
		} catch (EmailException e) {
			throw new EmailException(e.getMessage(), e);
		}
	}

	private EmailMessage convertToIrisMessage(Message message) throws IOException, MessagingException {
		MimeMessage m = (MimeMessage) message;

		from = m.getFrom()[0].toString();
		from = from.substring(from.indexOf('<')+1);
		from = from.substring(0, from.length()-1);

		EmailMessage msg = new EmailMessage();
		try {
			msg.setMessage(getText(m));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		msg.setBcc(convertAddressToString(m.getRecipients(RecipientType.BCC)));
		msg.setCc(convertAddressToString(m.getRecipients(RecipientType.CC)));
		msg.setTo(convertAddressToString(m.getRecipients(RecipientType.TO)));
		msg.setFrom(from);
		msg.setSubject(m.getSubject());
		msg.setDate(m.getReceivedDate());
		
		return msg;
	}
	
	
	protected String getText(Object content) throws Exception {
		try {
			Method method = getClass().getDeclaredMethod("getText", content.getClass());
			return method.invoke(this, content).toString();
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("can't read message type: " + content.getClass());
		}
	}

	protected String getText(javax.mail.internet.MimeMultipart multi) throws Exception {
		System.out.println("getText: MimeMultipart :: "+MessageUtils.getMimeType(multi.getContentType()));
		String contentType = MessageUtils.getMimeType(multi.getContentType());
		if (contentType.contains("multipart/signed")) {
			return getText(verifySignature(multi));
		}
		if (multi instanceof Multipart) {
			return getText((Multipart) multi);
		}
		//TODO ...
		return getText(multi);
	}

	protected String getText(Multipart multi) throws Exception {
		System.out.println("getText: Multipart :: "+MessageUtils.getMimeType(multi.getContentType()));
		int parts = multi.getCount();
		for (int j = 0; j < parts; j++) {
			MimeBodyPart part = (MimeBodyPart) multi.getBodyPart(j);
			System.out.println("getText: Multipart :: contentClass: "+MessageUtils.getMimeType(part.getContentType()));
			return getText(part.getContent());
		}
		//TODO ...
		return getText(multi);
	}

	protected String getText(MimeMessage msg) throws Exception {
		System.out.println("getText: MimeMessage :: "+MessageUtils.getMimeType(msg.getContentType()));
		String contentType = MessageUtils.getMimeType(msg.getContentType());
		if (contentType.contains("multipart/encrypted")) {
			return getText(decrypt(msg).getContent());
		}
		if (contentType.contains("multipart/signed")) {
			boolean signatureVerified = verifySignature(msg);
			System.out.println("signatureVerified: "+signatureVerified);
			if(signatureVerified){
				return getText(msg.getContent());
			}else{
				throw new EmailException("Invalid signature");
			}
		}
		return getText(msg.getContent());
	}
	
	protected String getText(InputStream msg) {
		System.out.println("getText: InputStream");
		System.out.println("InputStream: "+msg);
		return MessageUtils.getStringFromInputStream(msg);
	}
	
	protected String getText(String str) {
		System.out.println("getText: String");
		//System.out.println(str);
		return str;
	}
	
	private MimeMessage decrypt(MimeMessage m) throws Exception{
		System.out.println("received encrypted message ...");
		return PgpManager.instance().decrypt(session.getSession(), m);
	}
	
	private boolean verifySignature(MimeMessage msg) throws Exception{
		return PgpManager.instance().verifySignature(session.getSession(), msg);
	}
	private String verifySignature(MimeBodyPart part) throws Exception {
		boolean signatureVerified = PgpManager.instance().verifySignature(session.getSession(), part, from);
		System.out.println("signatureVerified: "+signatureVerified);
		if(signatureVerified){
			return getText(part.getContent());
		}
		throw new EmailException("Invalid signature");
	}
	private String verifySignature(MimeMultipart multi) throws Exception {
		boolean signatureVerified = PgpManager.instance().verifySignature(session.getSession(), multi, from);
		System.out.println("signatureVerified: "+signatureVerified);
		if(signatureVerified){
			return getText((Multipart)multi);
		}
		throw new EmailException("Invalid signature");
	}

	private String convertAddressToString(Address[] recipients) {
		StringBuilder sb = new StringBuilder("");
		if (recipients != null) {
			for (Address a : recipients) {
				sb.append(a.toString() + ", ");
			}
		}
		return sb.toString();
	}

	private Store createStoreAndConnect() throws MessagingException {
		System.out.println("Creating store ...");
		Store store = session.getSession().getStore(provider.getStoreProtocol());
		store.addStoreListener(this);
		store.addConnectionListener(session);
		session.connect(store, provider.getStoreHost(), provider.getStorePort());
		return store;
	}

	public Store getStore() throws EmailException {
		if (store == null) {
			try {
				store = createStoreAndConnect();
			} catch (MessagingException e) {
				throw new EmailException(e.getMessage(), e);
			}
		}
		return store;
	}

	public Store renew() throws EmailException {
		if (store != null) {
			try {
				store.close();
			} catch (MessagingException e) {
				throw new EmailException(e.getMessage(), e);
			}
			store = null;
		}
		return getStore();
	}

	@Override
	public void notification(StoreEvent e) {
		System.out.println("Notification: " + e.getMessage());
	}

	@Override
	public void folderCreated(FolderEvent e) {
	}

	@Override
	public void folderDeleted(FolderEvent e) {
	}

	@Override
	public void folderRenamed(FolderEvent e) {
	}
}