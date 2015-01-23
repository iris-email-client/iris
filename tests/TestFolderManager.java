package br.unb.cic.iris.core;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import br.unb.cic.iris.core.model.IrisFolder;
import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.persistence.IEmailDAO;
import br.unb.cic.iris.persistence.IFolderDAO;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFolderManager {
	
	private IEmailDAO emailDao;
	private IFolderDAO folderDao;
	
	private EmailMessage message;
	private EmailMessage message2;
	private IrisFolder folder;
	private IrisFolder folder2;
	
	private final static String FOLDER_NAME1 = UUID.randomUUID().toString();
	private final static String FOLDER_NAME2 = UUID.randomUUID().toString();
	private final static String SUBJECT = UUID.randomUUID().toString();
	
	@Before
	public void setup() throws Exception{
		
		try {

			emailDao = SystemFacade.instance().getDaoFactory().createEmailDAO();
			folderDao = SystemFacade.instance().getDaoFactory().createFolderDAO();
			
			folder = folderDao.findByName(FOLDER_NAME1);
			if (folder == null) {
				folder = new IrisFolder(FOLDER_NAME1);
				folderDao.saveOrUpdate(folder);
			}
			
			message = new EmailMessage("email-to-test@test.com", "email-to-test@test.com", "email-to-test@test.com", "email-to-test@test.com", SUBJECT, "test message 1");
			message.setFolder(folder);
			emailDao.saveMessage(message);
			
			folder2 = folderDao.findByName(FOLDER_NAME2);
			if (folder2 == null) {
				folder2 = new IrisFolder(FOLDER_NAME2);
				folderDao.saveOrUpdate(folder2);
			}
			
			message2 = new EmailMessage("email-to-test2@test.com", "email-to-test2@test.com", "email-to-test2@test.com", "email-to-test2@test.com", SUBJECT, "test message 2");
			message2.setFolder(folder2);
			emailDao.saveMessage(message2);
			
		} catch (Exception e) {
			throw new Exception("Faild while setting up Folder Test.", e);
		}
		
	}
	
	@After
	public void tearDown() throws Exception{
		
		try {
			
			emailDao.delete(message);
			emailDao.delete(message2);
			folderDao.delete(folder);
			folderDao.delete(folder2);
			
		} catch (Exception e) {
			throw new Exception("Faild while tearing down Folder Test.", e);
		}
		
	}
	
	@Test
	public void testChangeToFolder() throws Exception {
		
		try {
			
			String oldFolderName = FolderManager.instance().getCurrentFolderName();
			IrisFolder folder = FolderManager.instance().changeToFolder(this.folder2.getId());
			String newFolderName = FolderManager.instance().getCurrentFolderName();
			Boolean sameFolder = (oldFolderName.equals(newFolderName));
			
			Assert.assertTrue("Folder is not consistent between two calls", folder.getName().equals(newFolderName));
			Assert.assertTrue("Changing from folder A to B faild", !sameFolder);
			Assert.assertTrue("Changed to an unexpected folder", newFolderName.equals(FOLDER_NAME2));
			
			
		} catch (Exception e) {
			throw new Exception("Faild while changing from folder A to folder B", e);
		}
		
		
	}
	
	@Test
	public void testListFolders() throws Exception{
		
		try{
			List<IrisFolder> folders = FolderManager.instance().listFolders();
			
			Boolean correctFolder = true;
			Integer counter = 0;
			for(IrisFolder folder : folders){
				if(!folder.getName().equals(FOLDER_NAME1) && !folder.getName().equals(FOLDER_NAME2))
					correctFolder = false;
				counter++;
					
			}
			
			Assert.assertTrue("Folders that are not suposed to exist were found", correctFolder);
			Assert.assertTrue("One or more folders werent retrived as it should", counter==2);
			Assert.assertTrue("Duplicated folder retrieved", counter<=2 && correctFolder);
	
		}
		catch(Exception e){
			throw new Exception("Faild while listing all folders", e);
		}
	}
	
	@Test
	public void testListFolderMessages() throws Exception {
		try{
			
			FolderManager.instance().changeToFolder(this.folder.getId());
			List<EmailMessage> messages = FolderManager.instance().listFolderMessages();
			
			Boolean correctMessage = true;
			Integer counter = 0;
			for(EmailMessage message : messages){
				if(!message.getSubject().equals(SUBJECT))
					correctMessage = false;
				counter++;
			}
			
			Assert.assertTrue("Folder cointais messages that shouldnt be there.", correctMessage);
			Assert.assertTrue("One or more messages werent retrived as it should.", counter==1);
			Assert.assertTrue("Duplicated message retrieved", counter==1 && correctMessage);
		}
		catch(Exception e){
			throw new Exception("Faild while listing all messages from one folder", e);
		}
		
	}

}
