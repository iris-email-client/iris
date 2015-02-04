package br.unb.cic.iris.mail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMEUtil;

import br.unb.cic.iris.security.KeystoreManager;

import com.sun.mail.util.BASE64DecoderStream;

public class EmailReader {
	/*
	 * Others GMail folders : [Gmail]/All Mail This folder contains all of your Gmail messages. [Gmail]/Drafts Your drafts. [Gmail]/Sent Mail Messages you sent to other people.
	 * [Gmail]/Spam Messages marked as spam. [Gmail]/Starred Starred messages. [Gmail]/Trash Messages deleted from Gmail.
	 */
	public static final String INBOX = "Inbox";

	Message message;
	Session session;

	private Store connect(String user, String password) throws MessagingException {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		session = Session.getDefaultInstance(props, null);
		// session.setDebug(true);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", user, password);
		return store;
	}

	public void showMessages(String user, String password, String folderName, int x) throws Exception {
		Store store = connect(user, password);
		Folder folder = store.getFolder(INBOX);
		folder.open(Folder.READ_WRITE);
		Message messages[] = folder.getMessages();

		// Get the last X messages
		int end = folder.getMessageCount();
		int start = end - x + 1;
		messages = reverseMessageOrder(folder.getMessages(start, end));

		System.out.println("No of Messages : " + folder.getMessageCount());
		System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
		for (int i = 0; i < messages.length; ++i) {
			message = messages[i];
			showMessage(message);
		}
	}

	public String getMimeType(String contentType) {
		String str = "unknown";
		StringTokenizer st = new StringTokenizer(contentType.trim(), ";");
		if (st.hasMoreTokens()) {
			str = st.nextToken();
			// str should look like:
			// text/plain, text/html, multipart/mixed, multipart/alternative,
			// multipart/related, or application/octec-stream, etc.
		}

		return str.toLowerCase();
	}

	private void showMessage(Message msg) throws Exception {
		try {
			System.out.println("\n\nMESSAGE #" + (msg.getMessageNumber()) + ":" + getMimeType(msg.getContentType()) + " ----" + msg.getClass() + " ---- content="
					+ msg.getContent().getClass());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String from = getFrom(msg);
		String subject = msg.getSubject();

		System.out.println("Message: " + from + " --- " + subject);
		showMessage(msg.getContent());
	}

	protected void showMessage(Object content) throws Exception {
		//System.out.println("Showing message: Object");
		//System.out.println("clazz=" + getClass());
		try {
			Method method = getClass().getDeclaredMethod("showMessage", content.getClass());
			method.invoke(this, content);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("can't read message type: " + content.getClass());
		}
	}

	protected void showMessage(javax.mail.internet.MimeMultipart multi) throws Exception {
		System.out.println("Showing message: MimeMultipart");
		if (multi instanceof Multipart) {
			showMessage((Multipart) multi);
		}
	}

	protected void showMessage(Multipart multi) throws Exception {
		System.out.println("Showing message: Multipart");
		int parts = multi.getCount();
		for (int j = 0; j < parts; ++j) {
			MimeBodyPart part = (MimeBodyPart) multi.getBodyPart(j);
			showMessage(part.getContent());
		}
	}

	protected void showMessage(String msg) {
		System.out.println("Showing message: String");
		System.out.println(msg);
	}

	protected void showMessage(BASE64DecoderStream msg) throws Exception {
		System.out.println("Showing message: BASE64DecoderStream");
		String from = getFrom(message);
		System.out.println("........FROM: " + from);

		// TODO esta usando o 'iris' ... tem que pegar o remetente
		MimeMultipart decryptedPart = decrypt("iris", msg);

		Object content = readSigned(decryptedPart);
		
		System.out.println("content_clazz="+content.getClass());
		System.out.println("------------ BEGIN ------------");
		System.out.println(content);
		System.out.println("------------- END -------------\n\n");
	}

	private MimeMultipart decrypt(String keyAlias, InputStream stream) throws Exception {
		KeystoreManager manager = KeystoreManager.instance();
		X509Certificate cert = (X509Certificate) manager.getKeystore().getCertificate(keyAlias);
		System.out.println("cert=" + cert);
		RecipientId recId = new JceKeyTransRecipientId(cert);
		System.out.println("RecipientId=" + recId);

		// Get a Session object with the default properties.
		/*
		 * Properties props = System.getProperties(); props.setProperty("mail.store.protocol", "imaps"); Session session = Session.getDefaultInstance(props, null);
		 */
		// MimeMessage msg = new MimeMessage(session, new FileInputStream("/home/pedro/tmp/iris/smime.p7m"));
		//MimeMessage msg = new MimeMessage(session, stream);
		//System.out.println("MSG="+msg);
		//SMIMEEnveloped m = new SMIMEEnveloped(msg);
		CMSEnvelopedData m = new CMSEnvelopedData(stream);

		RecipientInformationStore recipients = m.getRecipientInfos();
		RecipientInformation recipient = recipients.get(recId);
		System.out.println("RecipientInformation=" + recipient);

		MimeBodyPart res = SMIMEUtil.toMimeBodyPart(recipient.getContent(new JceKeyTransEnvelopedRecipient(manager.getPrivateKey(keyAlias)).setProvider(KeystoreManager.PROVIDER)));

		System.out.println("Message Contents");
		System.out.println("----------------");
		System.out.println(res.getContent());
		return (MimeMultipart) res.getContent();
	}

	//escreve o anexo da mensagem encriptada (smime.p7) em um arquivo local
	private void writeFile(String path, InputStream in) throws Exception {
		System.out.println("Writing encrypted message to: " + path);
		try (ReadableByteChannel inChannel = Channels.newChannel(in); 
				FileOutputStream fos = new FileOutputStream(path); 
				FileChannel fco = fos.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			while (true) {
				int read = inChannel.read(buffer);
				if (read == -1)
					break;
				buffer.flip();
				fco.write(buffer);
				buffer.clear();
			}
		}
	}

	private Object readSigned(MimeMultipart msg) throws Exception {
		SMIMESigned s = new SMIMESigned(msg);

		verify(s);

		// extract the content
		MimeBodyPart content = s.getContent();
		return content.getContent();
	}

	private static void verify(SMIMESigned s) throws Exception {
		// extract the information to verify the signatures.

		// certificates and crls passed in the signature
		org.bouncycastle.util.Store certs = s.getCertificates();
		System.out.println("CERTS: " + certs);

		// SignerInfo blocks which contain the signatures
		SignerInformationStore signers = s.getSignerInfos();

		Collection c = signers.getSigners();
		Iterator it = c.iterator();

		// check each signer
		while (it.hasNext()) {
			SignerInformation signer = (SignerInformation) it.next();
			System.out.println("signer=" + signer);
			Collection certCollection = certs.getMatches(signer.getSID());

			Iterator certIt = certCollection.iterator();
			X509Certificate cert = new JcaX509CertificateConverter().setProvider(KeystoreManager.PROVIDER).getCertificate((X509CertificateHolder) certIt.next());
			System.out.println("CERT=" + cert);
			
			// verify that the sig is correct and that it was generated
			// when the certificate was current
			if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(KeystoreManager.PROVIDER).build(cert))) {
				System.out.println("signature verified");
			} else {
				System.err.println("signature failed!");
				throw new RuntimeException("signature verification failed!");
			}
		}
	}

	private static Message[] reverseMessageOrder(Message[] msgs) {
		Message revMessages[] = new Message[msgs.length];
		int i = msgs.length - 1;
		for (int j = 0; j < msgs.length; j++, i--) {
			revMessages[j] = msgs[i];

		}
		return revMessages;
	}

	private String getFrom(Message msg) throws MessagingException {
		String from = "unknown";
		if (msg.getReplyTo().length >= 1) {
			from = msg.getReplyTo()[0].toString();
		} else if (msg.getFrom().length >= 1) {
			from = msg.getFrom()[0].toString();
		}
		return from;
	}
	
	
	public static void main(String[] args) {
		String user = "xxx@gmail.com";
		String password = "xxx";
		String folderName = INBOX;
		System.out.println("main .........");
		try {
			new EmailReader().showMessages(user, password, folderName, 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
