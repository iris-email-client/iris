package br.unb.cic.iris.mail;

import javax.mail.internet.MimeMessage;

import br.unb.cic.iris.security.PgpManager;


public class EmailSenderPgp extends EmailSender {

	protected MimeMessage createMessage(String to, String subject, String text) throws Exception {
		//TODO assinar e modificar header ... alem de pegar a chave publica do destino para encriptar

		/* Encrypt the message */
		String encryptedText = PgpManager.encrypt(text);
		
		/* Create a simple message, containing the body (the message itself) */
		MimeMessage body = super.createMessage(to, subject, encryptedText);

		/* return message ready to be sent */
		return body;
	}

	
}
