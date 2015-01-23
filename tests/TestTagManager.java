package br.unb.cic.iris.core;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import br.unb.cic.iris.core.model.EmailMessage;
import br.unb.cic.iris.core.model.IrisFolder;
import br.unb.cic.iris.persistence.IEmailDAO;
import br.unb.cic.iris.persistence.IFolderDAO;
import br.unb.cic.iris.tag.TagManager;
import br.unb.cic.iris.tag.model.Tag;
import br.unb.cic.iris.tag.persistence.ITagDAO;


public class TestTagManager {
	
	
	private IEmailDAO emailDao;
	private ITagDAO tagDao;
	private IFolderDAO folderDao;
	
	private EmailMessage message;
	private IrisFolder folder;
	
	
	private final static String TAG_NAME1 = UUID.randomUUID().toString();
	private final static String TAG_NAME2 = UUID.randomUUID().toString();
	private final static String TAG_NAME3 = UUID.randomUUID().toString();
	private final static String SUBJECT = UUID.randomUUID().toString();
	private final static String TEST_FOLDER_NAME = UUID.randomUUID().toString();
	
	@Before
	public void setup() throws Exception{
		
		try {

			emailDao = SystemFacade.instance().getDaoFactory().createEmailDAO();
			folderDao = SystemFacade.instance().getDaoFactory().createFolderDAO();
			tagDao = SystemFacade.instance().getDaoFactory().createTagDAO();
			
			folder = folderDao.findByName(TEST_FOLDER_NAME);
			if (folder == null) {
				folder = new IrisFolder(TEST_FOLDER_NAME);
				folderDao.saveOrUpdate(folder);
			}
			
			message = new EmailMessage("email-to-test@test.com", "email-to-test@test.com", "email-to-test@test.com", "email-to-test@test.com", SUBJECT, "test message 1");
			message.setFolder(folder);
			emailDao.saveMessage(message);
			message = new EmailMessage("email-to-test2@test.com", "email-to-test2@test.com", "email-to-test2@test.com", "email-to-test2@test.com", SUBJECT, "test message 2");
			message.setFolder(folder);
			emailDao.saveMessage(message);
			
			// finish creating msgs.
			
			// Add tags to messages.
			FolderManager.instance().changeToFolder(folder.getId());
			List<EmailMessage> messages = FolderManager.instance().listFolderMessages();
			
			Integer counter = 0;
			for(EmailMessage message : messages){
				if(counter == 0)
					TagManager.instance().saveTags(message.getId(), TAG_NAME1);
				else
					TagManager.instance().saveTags(message.getId(), TAG_NAME3);
				TagManager.instance().saveTags(message.getId(), TAG_NAME2);
				counter++;
			}
			
		} catch (Exception e) {
			throw new Exception("Faild while setting the test up!", e);
		}
		
	}
	
	@After
	public void tearDown() throws Exception{
		
		try {
			
			List<EmailMessage> messages = FolderManager.instance().listFolderMessages();
			
			for(EmailMessage message : messages){
				emailDao.delete(message);
			}
			
			List<Tag> tags = TagManager.instance().findAll();
			
			for(Tag tag : tags){
				tagDao.delete(tag);
			}
			folderDao.delete(folder);
			
		} catch (Exception e) {
			throw new Exception("Faild while tearing down the test!", e);
		}
		
	}
	
	@Test
	public void testFindAllTags() throws Exception{
		try {
			
			List<Tag> tags = TagManager.instance().findAll();
			
			Assert.assertTrue("One or more tags havent been found. ",(tags.contains(new Tag(TAG_NAME1)) && tags.contains(new Tag(TAG_NAME2)) && tags.contains(new Tag(TAG_NAME3))));

		} catch (Exception e) {
			throw new Exception("Faild while finding all tags", e);
		}
	}
	
	@Test
	public void testListMessagesByTag() throws Exception{
		try {
			
			List<EmailMessage> messages = TagManager.instance().listMessagesByTag(TAG_NAME2);
			
			Boolean correctMessage = true;
			for(EmailMessage LItem : messages){
				if(!LItem.getSubject().equals(SUBJECT)){
						correctMessage = false;
						break;
				}
			}
			
			
			Assert.assertTrue("One or more messages werent retrived",messages.size() == 2);
			Assert.assertTrue("Wrong message retrived", correctMessage );
			
		} catch (Exception e) {
			throw new Exception("Faild while finding all tags", e);
		}
	}
	
}
