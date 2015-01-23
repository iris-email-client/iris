package br.unb.cic.iris.core;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.unb.cic.iris.addressbook.model.AddressBookEntry;

public class TestAddressBook {
	
	private final static String NICK1 = "rbonifacio";
	private final static String EMAIL1 = "rbonifacio@cic.unb.br";
	private final static String NICK2 = "psalum";
	private final static String EMAIL2 = "psalum@me.com";
	
	@Before
	public void testSaveAddressBookEntry() {
		try {
			SystemFacade facade = SystemFacade.instance();
		
			AddressBookEntry entry = facade.find(NICK1);
			if(entry != null) {
				facade.deleteAddressBookEntry(NICK1);
			}
			entry = facade.find(NICK2);
			if(entry != null) {
				facade.deleteAddressBookEntry(NICK2);
			}
			
			System.out.println(" saving address book");
			facade.addAddressBookEntry(NICK1, EMAIL1);
			facade.addAddressBookEntry(NICK2, EMAIL2);
		
			entry = facade.find(NICK1);
			Assert.assertNotNull(entry);
			Assert.assertEquals(EMAIL1, entry.getAddress());
			entry = facade.find(NICK2);
			Assert.assertNotNull(entry);
			Assert.assertEquals(EMAIL2, entry.getAddress());
		}
		catch(Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testListAll() throws Exception{
		
		try {
			
			List<AddressBookEntry> entries = SystemFacade.instance().listAddressBook();
			
			Boolean correctEntry = true;			
			for(AddressBookEntry entry : entries){
				if(!entry.getNick().equals(NICK1) && !entry.getNick().equals(NICK2))
					correctEntry = false;
			}
			
			Assert.assertTrue("One or more entries werent retrieved",correctEntry);
			
		}catch (Exception e) {
			throw new Exception("Faild while listing all Address Book entries.", e);
		}
		
	}
	@After
	public void testDelete()throws Exception{
		
		try {
			
			SystemFacade.instance().deleteAddressBookEntry(NICK1);
			SystemFacade.instance().deleteAddressBookEntry(NICK2);
			
			Assert.assertNull("First entry was found after deleting it.",SystemFacade.instance().find(NICK1));
			Assert.assertNull("Seccond entry was found after deleting it.",SystemFacade.instance().find(NICK2));
			
		} catch (Exception e) {
			throw new Exception("Faild while deleting entries from Address Book.", e);
		}
		
	}
}
