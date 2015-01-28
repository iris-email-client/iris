package br.unb.cic.iris.security;

import static br.unb.cic.iris.security.KeystoreManager.DEFAULT_KEYSTORE_ALIAS;
import static br.unb.cic.iris.security.KeystoreManager.PROVIDER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.Strings;

/* Procedimento para criar o keystore na mao, caso necessario
 * https://www.sslshopper.com/article-most-common-java-keytool-keystore-commands.html
 * 
 * to generate keystore and key pair (inside USER_HOME/.iris/):
 * 
 * CREATE:
 * keytool -genkey -alias iris -keystore iris_keystore.pfx -storepass 123456 -validity 365 -keyalg RSA -keysize 2048 -storetype pkcs12
 * 
 * LIST:
 * keytool -list -keystore iris_keystore.pfx -storetype pkcs12
 */
public class EmailSenderSmime extends EmailSender {
	private KeystoreManager manager;

	//TODO temporario ... tem q pegar o email do usr q esta usando a app (enviando email)
	String keyAlias = DEFAULT_KEYSTORE_ALIAS;

	public EmailSenderSmime() {
		manager = new KeystoreManager();

		MailcapCommandMap mailcap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mailcap.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
		mailcap.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
		mailcap.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
		mailcap.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
		mailcap.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");
		CommandMap.setDefaultCommandMap(mailcap);
	}

	@Override
	protected MimeMessage createMessage(String to, String subject, String text) throws Exception {
		System.out.println("Creating encryted message to: " + to);

		/* get certificate chain */
		//TODO pegar o keyalias certo ... do usr q esta usando a app
		Certificate[] chain = manager.getCertificateChain(keyAlias);

		/* Create a simple message, containing the body (the message itself) */
		MimeMessage body = super.createMessage(to, subject, text);

		/* Sign the message */
		MimeMessage signedMessage = signMessage(body, chain, body.getAllHeaderLines());

		/* Encrypt the message */
		MimeMessage encryptedMessage = encryptMessage(signedMessage, chain, body.getAllHeaderLines());

		/* return message ready to be sent */
		return encryptedMessage;
	}

	private MimeMessage signMessage(MimeMessage body, Certificate[] chain, Enumeration headers) throws Exception {
		System.out.println("Signing message ...");
		/* Get the private key to sign the message with */
		PrivateKey privateKey = manager.getPrivateKey(keyAlias);

		/* Create the SMIMESignedGenerator */
		SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
		capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
		capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
		capabilities.addCapability(SMIMECapability.dES_CBC);

		ASN1EncodableVector attributes = new ASN1EncodableVector();
		attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(new IssuerAndSerialNumber(new X500Name(((X509Certificate) chain[0]).getIssuerDN().getName()),
				((X509Certificate) chain[0]).getSerialNumber())));
		attributes.add(new SMIMECapabilitiesAttribute(capabilities));

		SMIMESignedGenerator signer = new SMIMESignedGenerator();
		signer.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider(PROVIDER).setSignedAttributeGenerator(new AttributeTable(attributes))
				.build("DSA".equals(privateKey.getAlgorithm()) ? "SHA1withDSA" : "MD5withRSA", privateKey, (X509Certificate) chain[0]));

		/* Add the list of certs to the generator */
		List certList = new ArrayList();
		certList.add(chain[0]);
		Store certs = new JcaCertStore(certList);
		signer.addCertificates(certs);

		/* Sign the message */
		MimeMultipart mm = signer.generate(body, PROVIDER);
		MimeMessage signedMessage = new MimeMessage(getSession());

		/* Set all original MIME headers in the signed message */
		while (headers.hasMoreElements()) {
			signedMessage.addHeaderLine((String) headers.nextElement());
		}

		/* Set the content of the signed message */
		signedMessage.setContent(mm);
		signedMessage.saveChanges();

		return signedMessage;
	}

	private MimeMessage encryptMessage(MimeMessage message, Certificate[] chain, Enumeration headers) throws Exception {
		System.out.println("Encrypting message ...");
		/* Create the encrypter */
		SMIMEEnvelopedGenerator encrypter = new SMIMEEnvelopedGenerator();
		encrypter.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator((X509Certificate) chain[0]).setProvider(PROVIDER));

		/* Encrypt the message */
		MimeBodyPart encryptedPart = encrypter.generate(message, new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC).setProvider(PROVIDER).build());

		/*
		 * Create a new MimeMessage that contains the encrypted and signed content
		 */
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encryptedPart.writeTo(out);

		MimeMessage encryptedMessage = new MimeMessage(getSession(), new ByteArrayInputStream(out.toByteArray()));

		/* Set all original MIME headers in the encrypted message */
		while (headers.hasMoreElements()) {
			String headerLine = (String) headers.nextElement();
			/*
			 * Make sure not to override any content-* headers from the original message
			 */
			if (!Strings.toLowerCase(headerLine).startsWith("content-")) {
				encryptedMessage.addHeaderLine(headerLine);
			}
		}

		return encryptedMessage;
	}
	
}
