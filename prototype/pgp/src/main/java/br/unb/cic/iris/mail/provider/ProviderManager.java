package br.unb.cic.iris.mail.provider;

import java.util.List;

import br.unb.cic.iris.core.BaseManager;
import br.unb.cic.iris.mail.EmailProvider;
import br.unb.cic.iris.mail.provider.GmailProvider;
/*** added by dBaseMail* modified by dGmailProvider
 */
public class ProviderManager {
	private static ProviderManager instance = new ProviderManager();
	private BaseManager<EmailProvider> manager;
	private ProviderManager() {
		manager = new BaseManager<EmailProvider>();
		doAddProviders();
	}
	/*** modified by dGmailProvider
	 */
	public void doAddProviders() {
		doAddProviders_original0();
		addProvider(new GmailProvider());
	}
	public static ProviderManager instance() {
		return instance;
	}
	@SuppressWarnings("boxing")
	public void addProvider(EmailProvider provider) {
		manager.add(provider.getName().trim(), provider);
	}
	@SuppressWarnings("boxing")
	public EmailProvider getProvider(String name) {
		return manager.get(name);
	}
	public List<EmailProvider> getProviders() {
		return manager.getAll();
	}
	/*** modified by dGmailProvider
	 */
	public void doAddProviders_original0() {
	}
}