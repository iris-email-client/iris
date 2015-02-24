package br.unb.cic.iris.mail;

import java.util.List;

import javax.mail.search.SearchTerm;

import br.unb.cic.iris.core.exception.EmailException;
import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.core.model.IrisFolder;
import br.unb.cic.iris.mail.pgp.SecurityType;

/***
 * added by dBaseMail
 */
public interface IEmailClient {
	public void send(EmailMessage message) throws EmailException;
	
	//NOVO
	public void send(EmailMessage message, SecurityType...types) throws EmailException;
	

	public List<IrisFolder> listFolders() throws EmailException;

	public List<EmailMessage> getMessages(String folder, SearchTerm searchTerm) throws EmailException;

	public List<EmailMessage> getMessages(String folder) throws EmailException;

	public List<String> validateEmailMessage(EmailMessage message);

	public List<EmailMessage> getMessages(String folder, int seqnum) throws EmailException;

	public List<EmailMessage> getMessages(String folder, int begin, int end) throws EmailException;
}