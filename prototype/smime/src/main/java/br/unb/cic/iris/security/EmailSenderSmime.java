package br.unb.cic.iris.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.Strings;

/*
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
	private static final String PROVIDER = "BC";
	private static final String TYPE = "PKCS12";
	String pkcs12Keystore = System.getProperty("user.home")+"/.iris/iris_keystore.pfx";
	String password = "123456";
	String keyAlias = "iris";

	protected MimeMessage createMessage(String to, String subject, String text) throws Exception {
		System.out.println("Creating message to: " + to);
		MailcapCommandMap mailcap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();

		mailcap.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
		mailcap.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
		mailcap.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
		mailcap.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
		mailcap.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");

		CommandMap.setDefaultCommandMap(mailcap);

		/* Add BC */
		Security.addProvider(new BouncyCastleProvider());

		/* Open the keystore */
		KeyStore keystore = KeyStore.getInstance(TYPE, PROVIDER);
		keystore.load(new FileInputStream(pkcs12Keystore), password.toCharArray());
		Certificate[] chain = keystore.getCertificateChain(keyAlias);

		/* Get the private key to sign the message with */
		PrivateKey privateKey = (PrivateKey) keystore.getKey(keyAlias, password.toCharArray());
		if (privateKey == null) {
			throw new Exception("cannot find private key for alias: " + keyAlias);
		}

		MimeMessage body = super.createMessage(to, subject, text);

		/* Create the SMIMESignedGenerator */
		SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
		capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
		capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
		capabilities.addCapability(SMIMECapability.dES_CBC);

		ASN1EncodableVector attributes = new ASN1EncodableVector();
		attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(
				new IssuerAndSerialNumber(new X500Name(((X509Certificate) chain[0]).getIssuerDN().getName()),
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
		Enumeration headers = body.getAllHeaderLines();
		while (headers.hasMoreElements()) {
			signedMessage.addHeaderLine((String) headers.nextElement());
		}

		/* Set the content of the signed message */
		signedMessage.setContent(mm);
		signedMessage.saveChanges();

		/* Create the encrypter */
		SMIMEEnvelopedGenerator encrypter = new SMIMEEnvelopedGenerator();
		encrypter.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator((X509Certificate) chain[0]).setProvider(PROVIDER));

		/* Encrypt the message */
		MimeBodyPart encryptedPart = encrypter.generate(signedMessage, new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC).setProvider(PROVIDER).build());

		/*
		 * Create a new MimeMessage that contains the encrypted and signed content
		 */
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encryptedPart.writeTo(out);

		MimeMessage encryptedMessage = new MimeMessage(getSession(), new ByteArrayInputStream(out.toByteArray()));

		/* Set all original MIME headers in the encrypted message */
		headers = body.getAllHeaderLines();
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

	public static void main(String[] args) {
		EmailSender sender = new EmailSenderSmime();
		String to = "XXX@gmail.com";
		String subject = "teste encriptado e assinado";
		String body = "teste 123";
		try {
			sender.sendMessage(to, subject, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
