package br.unb.cic.iris;

import java.util.Date;

import br.unb.cic.iris.core.SystemFacade;
import br.unb.cic.iris.core.exception.EmailException;
import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.mail.pgp.SecurityType;

public class MainSend {
	private static String from = "canarioc@gmail.com";
	private static String message = "teste 123 ...";
	private static String subject = "teste :: ";
	private static String to = "br.unb.cic.iris@gmail.com";

	static String getSubject(){
		return subject+new Date()+" :: ";
	}
	
	public static void main(String[] args) {
		String newSubject = "";
		EmailMessage msg = createEmailMessage(from, message, newSubject, to);

		try {
			// Invalid signature
			/*newSubject = subject + "encriptado e assinado";
			msg.setSubject(newSubject);
			SystemFacade.instance().send(msg, SecurityType.ENCRYPT, SecurityType.SIGN);
			System.out.println("\n\n");*/
			
			newSubject = getSubject() + "assinado e encriptado";
			msg.setSubject(newSubject);
			SystemFacade.instance().send(msg, SecurityType.SIGN, SecurityType.ENCRYPT);
			System.out.println("\n\n");

			newSubject = getSubject() + "encriptado";
			msg.setSubject(newSubject);
			SystemFacade.instance().send(msg, SecurityType.ENCRYPT);
			System.out.println("\n\n");

			newSubject = getSubject() + "assinado";
			msg.setSubject(newSubject);
			SystemFacade.instance().send(msg, SecurityType.SIGN);
			System.out.println("\n\n");
			
			newSubject = getSubject() + "texto 1";
			msg.setSubject(newSubject);
			SystemFacade.instance().send(msg, null);
			System.out.println("\n\n");
			
			newSubject = getSubject() + "texto 2";
			msg.setSubject(newSubject);
			SystemFacade.instance().send(msg);
		} catch (EmailException e) {
			e.printStackTrace();
		}
		
	}

	static EmailMessage createEmailMessage(String from, String message, String subject, String to) {
		EmailMessage msg = new EmailMessage();
		msg.setDate(new Date());
		msg.setFrom(from);
		msg.setMessage(message);
		msg.setSubject(subject);
		msg.setTo(to);
		return msg;
	}
}
