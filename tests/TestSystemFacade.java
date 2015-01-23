/*
 * TestSystemFacade.java
 * ---------------------------------
 *  version: 0.0.1
 *  date: Sep 18, 2014
 *  author: rbonifacio
 *  list of changes: (none) 
 */
package br.unb.cic.iris.core;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.core.model.IrisFolder;
import br.unb.cic.iris.mail.EmailProvider;


/**
 * @author ExceptionHandling
 *
 */
public class TestSystemFacade {

	private EmailProvider provider;
	private final static String SUBJECT = UUID.randomUUID().toString();
	private final static String TESTEMAIL = "irismailteste@gmail.com";
	private EmailMessage message = new EmailMessage(TESTEMAIL,TESTEMAIL,"","",SUBJECT,"Conteúdo de teste");
	
	@BeforeClass
	public static void testSend() throws Exception {
		
		final EmailMessage message = new EmailMessage(TESTEMAIL,TESTEMAIL,"","",SUBJECT,"Conteúdo de teste");
		
		try {
			
			SystemFacade.instance().send(message);
			SystemFacade.instance().send(message);
			
			
			
		} catch (Exception e) {
			throw new Exception("failed while sending message.", e);
		}
		
	}
	
//	@Test
//	public void tearDown() throws Exception {
//		
//		try {
//			
//			List<EmailMessage> messages = FolderManager.instance().listFolderMessages();
//			
//			//TODO: delete message created during the test.
//			
//			
//			
//		} catch (Exception e) {
//			throw new Exception("failed while sending message.", e);
//		}
//		
//	}
	
	//Test: Passed
	@After
	public void testDefaultProvider() throws Exception {
		try {
		
			EmailProvider provider = SystemFacade.instance().getProvider();
			Assert.assertNotNull(provider);
			
			
		} catch (Exception e) {
			throw new Exception("failed while getting to defout provider.", e);
		}
		
	}
	
	
	@Before
	public void testDownloadMessagesAndGetMessages() throws Exception{
		
		try {
			
			String folder = IrisFolder.INBOX;
			SystemFacade.instance().downloadMessages(folder);
			
			List<EmailMessage> messages = SystemFacade.instance().getMessages(folder);
			
			Boolean correctMessage = true;
			for(EmailMessage message : messages){
				
				if(!message.getSubject().equals(SUBJECT))
					correctMessage = false;
				
			}
			
			Assert.assertTrue("Wrong messages retrieved", correctMessage);
			
			
		} catch (Exception e) {
			throw new Exception("failed while downloading message.", e);
		}
		
	}
	
	
	@Test
	public void testListInboxMessages() throws Exception{
		
		try {
			
			List<EmailMessage> messages = SystemFacade.instance().listInboxMessages();
			
			Boolean correctMessage = true;
			Integer counter = 0;
			for(EmailMessage message : messages){
				if(message.getSubject().equals(SUBJECT))
					correctMessage = false;
				counter++;
				
			}
			
			Assert.assertTrue("Wrong message retrieved from inbox", correctMessage);
			Assert.assertTrue("One or more messages werent retrieved", counter==2);
			Assert.assertTrue("One or more messages retrieved are duplicated", counter<=2 && correctMessage);
			
		} catch (Exception e) {
			throw new Exception("failed while listing inbox messages.", e);
		}
		
	}
	
}
