package br.unb.cic.iris.security;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

import com.sun.mail.util.BASE64DecoderStream;

public class EmailReader {
	/*
	 * Others GMail folders : [Gmail]/All Mail This folder contains all of your Gmail messages. [Gmail]/Drafts Your drafts. [Gmail]/Sent Mail Messages you sent to other people.
	 * [Gmail]/Spam Messages marked as spam. [Gmail]/Starred Starred messages. [Gmail]/Trash Messages deleted from Gmail.
	 */
	public static final String INBOX = "Inbox";
	
	Message message;

	private Store connect(String user, String password) throws MessagingException {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = Session.getDefaultInstance(props, null);
		// session.setDebug(true);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", user, password);
		return store;
	}

	public void showMessages(String user, String password, String folderName) throws Exception {
		Store store = connect(user, password);
		Folder folder = store.getFolder(INBOX);
		folder.open(Folder.READ_WRITE);
		Message messages[] = folder.getMessages();
		System.out.println("No of Messages : " + folder.getMessageCount());
		System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
		for (int i = 0; i < messages.length; ++i) {
			message = messages[i];
			showMessage(message);
			// System.out.println("\n\nMESSAGE #" + (i + 1) + ":" + msg.getContentType()+" ----"+msg.getClass());

			/*
			 * if (msg.getContentType().contains("APPLICATION/PKCS7-MIME")) { System.out.println("encriptada ........................."); System.out.println(" ---> " +
			 * msg.getDescription()); System.out.println(" ---> " + msg.getDisposition()); System.out.println(" ---> " + msg.getFileName()); System.out.println(" ---> " +
			 * msg.getLineCount()); System.out.println(" ---> " + msg.getSize()); System.out.println(" ---> " + msg.getContent()); try {
			 * writeFile("/home/pedro/tmp/iris/"+msg.getFileName(), msg.getInputStream()); } catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); } }
			 */

			/*
			 * if we don''t want to fetch messages already processed if (!msg.isSet(Flags.Flag.SEEN)) { String from = "unknown"; ... }
			 */

			// you may want to replace the spaces with "_"
			// the TEMP directory is used to store the files
			// String filename = "/home/pedro/tmp/iris/" + subject;
			// saveParts(msg.getContent(), filename);
			// msg.setFlag(Flags.Flag.SEEN, true);
			// to delete the message
			// msg.setFlag(Flags.Flag.DELETED, true);
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

		return str.toLowerCase(); // iPlanet uses Upper case for mime type
	}

	private void showMessage(Message msg) throws Exception {
		try {
			System.out.println("\n\nMESSAGE #" + (msg.getMessageNumber()) + ":" + getMimeType(msg.getContentType()) + " ----" + msg.getClass() + " ---- content=" + msg.getContent().getClass());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String from = "unknown";
		if (msg.getReplyTo().length >= 1) {
			from = msg.getReplyTo()[0].toString();
		} else if (msg.getFrom().length >= 1) {
			from = msg.getFrom()[0].toString();
		}
		String subject = msg.getSubject();
		
		System.out.println("Message: " + from + " --- " + subject);
		showMessage(msg.getContent());
	}

	protected void showMessage(Object content) throws Exception {
		System.out.println("Showing message: Object");
		System.out.println("clazz="+getClass());
		try{
			Method method = getClass().getDeclaredMethod("showMessage", content.getClass());
			method.invoke(this, content);
		}catch(NoSuchMethodException ex){
			throw new RuntimeException("can't read message type: "+content.getClass());
		}
	}

	protected void showMessage(javax.mail.internet.MimeMultipart multi) throws Exception {
		if(multi instanceof Multipart){
			showMessage((Multipart)multi);
		}
	}
	protected void showMessage(Multipart multi) throws Exception {
		System.out.println("Showing message: MimeMultipart");
		int parts = multi.getCount();
		for (int j = 0; j < parts; ++j) {
			MimeBodyPart part = (MimeBodyPart) multi.getBodyPart(j);
			showMessage(part.getContent());
			/*
			 * if (part.getContent() instanceof Multipart) { // part-within-a-part, do some recursion... showMessage(part.getContent()); } else { System.out.println("MIME=" +
			 * part.getContentType() + "   ---- desc=" + part.getDescription() + "  ----- file=" + part.getFileName() + "  ---- class="+ part.getClass());
			 * System.out.println("CONTENT="+part.getContent().getClass()); //TODO no iris, salvar o tipo de mensagem? if (part.isMimeType("text/html")) {
			 * System.out.println("HTML ...."); } else if (part.isMimeType("text/plain")) { System.out.println("TEXT ....."); }
			 * 
			 * showMessage(part.getContent()); }
			 */
		}
	}
	protected void showMessage(String msg) {
		System.out.println("Showing message: String");
		System.out.println(msg);
	}
	
	//com.sun.mail.util.BASE64DecoderStream
	protected void showMessage(BASE64DecoderStream msg) throws Exception{
		System.out.println("Showing message: BASE64DecoderStream");
		String path = "/home/pedro/tmp/iris/"+message.getFileName();
		writeFile(path, msg);
	}
	
	private void writeFile(String path, InputStream in) throws Exception {
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

	public static void main(String[] args) {
		String user = "xxx@gmail.com";
		String password = "xxx";
		String folderName = INBOX;
		try {
			new EmailReader().showMessages(user, password, folderName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
