package br.unb.cic.iris.mail.pgp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchProviderException;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import net.suberic.crypto.EncryptionManager;
import net.suberic.crypto.EncryptionUtils;
import br.unb.cic.iris.core.SystemFacade;
import br.unb.cic.iris.core.exception.EmailUncheckedException;

public class PgpManager {
	private static final PgpManager instance = new PgpManager();

	private PgpKeyManager keyManager;
	private EncryptionUtils cryptoUtils;

	public PgpManager() {
		try {
			keyManager = new PgpKeyManager();
			cryptoUtils = getEncryptionUtils();
		} catch (NoSuchProviderException e) {
			throw new EmailUncheckedException("No such provider: " + e.getMessage(), e);
		} catch (FileNotFoundException e) {
			throw new EmailUncheckedException("Config file not found: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new EmailUncheckedException("Error reading config file: " + e.getMessage(), e);
		}
	}

	public static PgpManager instance() {
		return instance;
	}

	public MimeMessage encrypt(Session mailSession, MimeMessage msg, String publicKeyAlias) throws Exception {
		System.out.println("Encrypting message to: " + publicKeyAlias);
		java.security.Key pgpPublicKey = keyManager.getPublicKey(publicKeyAlias);
		System.out.println("pgpPublicKey: " + pgpPublicKey);
		return cryptoUtils.encryptMessage(mailSession, msg, pgpPublicKey);
	}

	public MimeMessage decrypt(MimeMessage msg, String privateKeyAlias, Session mailSession) throws Exception {
		System.out.println("Decrypting message: " + privateKeyAlias);
		java.security.Key pgpPrivateKey = keyManager.getPrivateKey(privateKeyAlias);
		return cryptoUtils.decryptMessage(mailSession, msg, pgpPrivateKey);
	}

	public MimeMessage decrypt(Session mailSession, MimeMessage msg) throws Exception {
		String to = msg.getRecipients(RecipientType.TO)[0].toString();
		to = to.substring(to.indexOf('<') + 1);
		to = to.substring(0, to.length() - 1);

		to = SystemFacade.instance().getProvider().getUsername();
		System.out.println("TO: " + to);
		return decrypt(msg, to, mailSession);
	}

	public MimeMessage sign(Session mailSession, MimeMessage msg, String alias) throws Exception {
		System.out.println("Signing message: " + alias);
		Key privateKey = keyManager.getPrivateKey(alias);
		System.out.println("privateKey=" + privateKey);
		return cryptoUtils.signMessage(mailSession, msg, privateKey);
	}

	public boolean verifySignature(Session mailSession, MimeMessage signedMsg) throws Exception {
		System.out.println("Verify signature ...");
		String from = signedMsg.getFrom()[0].toString();
		from = from.substring(from.indexOf('<') + 1);
		from = from.substring(0, from.length() - 1);
		System.out.println("FROM: " + from);

		Key publicKey = keyManager.getPublicKey(from);
		System.out.println("PUBLIC KEY: " + publicKey);

		return cryptoUtils.checkSignature(signedMsg, publicKey);
	}

	public boolean verifySignature(Session mailSession, MimePart part, String from) throws Exception {
		System.out.println("Verify signature ...");
		System.out.println("FROM: " + from);
		Key publicKey = keyManager.getPublicKey(from);
		System.out.println("PUBLIC KEY: " + publicKey);
		return cryptoUtils.checkSignature(part, publicKey);
	}

	public boolean verifySignature(Session session, MimeMultipart multi, String from) throws Exception {
		System.out.println("Verify signature ...");
		System.out.println("FROM: " + from);
		Key publicKey = keyManager.getPublicKey(from);
		System.out.println("PUBLIC KEY: " + publicKey);
		return cryptoUtils.checkSignature(multi, publicKey);
	}

	private EncryptionUtils getEncryptionUtils() throws NoSuchProviderException {
		return EncryptionManager.getEncryptionUtils(EncryptionManager.PGP);
	}

}
