package br.unb.cic.iris.mail;

import static br.unb.cic.iris.MainApp.*;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
	private Session session;

	public void sendMessage(String to, String subject, String body) throws Exception {
		send(createMessage(to, subject, body));
	}

	protected Session getSession() {
		if (session == null) {
			System.out.println("Opening session ...");
			session = Session.getInstance(getProperties(), new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		}
		return session;
	}

	private Properties getProperties() {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", 587);
		System.out.println("PROPERTIES: "+props.toString());
		return props;
	}

	protected MimeMessage createMessage(String to, String subject, String body) throws Exception {
		System.out.println("Creating message to: "+to+"; subject="+subject);
		MimeMessage message = new MimeMessage(getSession());
		message.setFrom(new InternetAddress(username));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject(subject);
		message.setText(body);
		message.saveChanges();
		return message;
	}

	private void send(Message message) throws MessagingException {
		System.out.println("Sending message ...");
		Transport.send(message);
		System.out.println("Message sent!");
	}

}
