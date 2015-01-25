package br.unb.cic.iris.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Random;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class KeystoreManager {
	private static final String ENCODING = "UTF-8";
	private static final String DEFAULT_KEYSTORE_FILE = System.getProperty("user.home") + "/.iris/iris_keystore_novo.pfx";
	private static final String DEFAULT_KEYSTORE_PASSWORD = "123456";
	private static final String DEFAULT_KEYSTORE_ALIAS = "iris";
	private static final String DEFAULT_KEYSTORE_TYPE = "PKCS12";
	private static final String DEFAULT_DN = "CN=Iris, OU=CIC, O=Universidade de Brasilia, L=Brasilia, ST=DF, C=BR";
	private static final String ASYMMETRIC_ALGORITHM = "RSA";
	private static final String PROVIDER = "BC";

	static{
		/* Add BC */
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public void exportCertificate(String alias, File certificateOutFile) throws Exception {
		KeyStore keystore = loadKeystore();

		Certificate cert = keystore.getCertificate(alias);

		byte[] buf = cert.getEncoded();

		FileOutputStream os = new FileOutputStream(certificateOutFile);
		os.write(buf);
		os.close();

		Writer wr = new OutputStreamWriter(os, Charset.forName(ENCODING));
		wr.write(new sun.misc.BASE64Encoder().encode(buf));
		wr.flush();

	}

	public KeyPair retrieveKeyPair(String alias) throws Exception {
		KeyStore keystore = loadKeystore();

		Key key = keystore.getKey(alias, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
		if (key instanceof PrivateKey) {
			// Get certificate of public key
			Certificate cert = keystore.getCertificate(alias);

			// Get public key
			PublicKey publicKey = cert.getPublicKey();

			// Return a key pair
			return new KeyPair(publicKey, (PrivateKey) key);
		}
		return null;
	}

	private KeyStore loadKeystore() throws Exception {
		System.out.println("Loading keystore ...");
		FileInputStream is = new FileInputStream(DEFAULT_KEYSTORE_FILE);
		KeyStore keystore = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE, PROVIDER);
		keystore.load(is, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
		return keystore;
	}

	private KeyPair createKeyPair() throws Exception {
		System.out.println("Creating key pair ...");
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM, PROVIDER);
		// keyGen.initialize(2048, SecureRandom.getInstanceStrong());
		keyGen.initialize(1024);
		KeyPair keypair = keyGen.generateKeyPair();
		// PrivateKey privKey = keypair.getPrivate();
		// PublicKey pubKey = keypair.getPublic();
		return keypair;
	}

	public void createDefaultKeyStore() throws Exception {
		KeyPair keyPair = createKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		Certificate trustCert = createCertificate(DEFAULT_DN, DEFAULT_DN, publicKey, privateKey);
		//Certificate[] outChain = { createCertificate("CN=pedro.costa", DEFAULT_DN, publicKey, privateKey), trustCert };
		Certificate[] outChain = { trustCert };

		System.out.println("Writing keystore ...");
		KeyStore outStore = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE, PROVIDER);
		outStore.load(null, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
		outStore.setKeyEntry(DEFAULT_KEYSTORE_ALIAS, privateKey, DEFAULT_KEYSTORE_PASSWORD.toCharArray(), outChain);
		OutputStream outputStream = new FileOutputStream(DEFAULT_KEYSTORE_FILE);
		outStore.store(outputStream, DEFAULT_KEYSTORE_PASSWORD.toCharArray());
		outputStream.flush();
		outputStream.close();
		System.out.println("Done!");
	}

	private static X509Certificate createCertificate(String dn, String issuer, PublicKey publicKey, PrivateKey privateKey) throws Exception {
		System.out.println("Creating certificate. DN=" + dn);
		X509V3CertificateGenerator certGenerator = new X509V3CertificateGenerator();
		certGenerator.setSerialNumber(BigInteger.valueOf(Math.abs(new Random().nextLong())));
		certGenerator.setIssuerDN(new X509Name(dn));
		certGenerator.setSubjectDN(new X509Name(dn));
		certGenerator.setIssuerDN(new X509Name(issuer)); // Set issuer!
		certGenerator.setNotBefore(Calendar.getInstance().getTime());
		certGenerator.setNotAfter(Calendar.getInstance().getTime());
		certGenerator.setPublicKey(publicKey);
		certGenerator.setSignatureAlgorithm("SHA1withRSA");
		X509Certificate certificate = (X509Certificate) certGenerator.generate(privateKey, PROVIDER);
		System.out.println("Certificate created!");
		return certificate;
	}

	public static void main(String[] args) {
		try {
			new KeystoreManager().createDefaultKeyStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
