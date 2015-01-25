package br.unb.cic.iris.security;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
	final String username = "XXX@gmail.com";
	final String password = "XXX";
	final String smtpServer = "smtp.gmail.com";
	final int smtpPort = 587;

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
		props.put("mail.smtp.host", smtpServer);
		props.put("mail.smtp.port", smtpPort+"");
		System.out.println("PROPERTIES: "+props.toString());
		return props;
	}

	protected MimeMessage createMessage(String to, String subject, String body) throws Exception {
		System.out.println("Creating message to: "+to);
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
	
	public static void main(String[] args) {
		EmailSender sender = new EmailSender();
		String to = "XXX@gmail.com";
		String subject = "teste simples";
		String body = "teste 123";
		try {
			sender.sendMessage(to, subject, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
