package br.unb.cic.iris.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class KeystoreManager {
	public static final String ENCODING = "UTF-8";
	public static final String DEFAULT_DN_SUFIX = ", OU=CIC, O=Universidade de Brasilia, L=Brasilia, ST=DF, C=BR";
	
	public static final String KEYSTORE_FILE = System.getProperty("user.home") + "/.iris/iris_keystore.pfx";
	public static final String KEYSTORE_PASSWORD = "123456";
	public static final String KEYSTORE_TYPE = "PKCS12";
	
	//funciona como CA ... vai assinar os certificados emitidos pelo iris
	public static final String ROOT_ALIAS = "iris";
	public static final String ROOT_DN = "CN="+ROOT_ALIAS+DEFAULT_DN_SUFIX;
	
	public static final String ASYMMETRIC_ALGORITHM = "RSA";
	public static final String PROVIDER = "BC";
	public static final int KEY_SIZE = 1024;
	
	private static final KeystoreManager instance = new KeystoreManager();
	
	KeyStore keystore;

	
	private KeystoreManager(){
		/* Add BC provider */
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public static KeystoreManager instance(){
		return instance;
	}
	
	public void exportCertificate(String alias, File certificateOutFile) throws Exception {
		KeyStore keystore = getKeystore();

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
		KeyStore keystore = getKeystore();

		Key key = keystore.getKey(alias, KEYSTORE_PASSWORD.toCharArray());
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
	
	public Certificate[] getCertificateChain(String keyAlias) throws Exception{
		//System.out.println("Certificate chain of: "+keyAlias);
		return getKeystore().getCertificateChain(keyAlias);
	}
	
	public PrivateKey getPrivateKey(String keyAlias) throws Exception {
		//System.out.println("private key of: "+keyAlias);
		PrivateKey privateKey = (PrivateKey) getKeystore().getKey(keyAlias, KEYSTORE_PASSWORD.toCharArray());
		if (privateKey == null) {
			throw new Exception("cannot find private key for alias: " + keyAlias);
		}
		return privateKey;
	}

	public KeyStore getKeystore() throws Exception {
		if(keystore == null){
			if(!keystoreExists()){
				createDefaultKeyStore();
			}
			System.out.println("Loading keystore: "+KEYSTORE_FILE);
			System.out.println("Keystore type: "+KEYSTORE_TYPE);
			FileInputStream is = new FileInputStream(KEYSTORE_FILE);
			keystore = KeyStore.getInstance(KEYSTORE_TYPE, PROVIDER);
			keystore.load(is, KEYSTORE_PASSWORD.toCharArray());
		}
		return keystore;
	}
	
	private void createDefaultKeyStore() {
		// TODO copy keystore to right place
		
	}

	private boolean keystoreExists(){
		return new File(KEYSTORE_FILE).exists();
	}

	private KeyPair createKeyPair() throws Exception {
		System.out.println("Creating key pair ...");
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM, PROVIDER);
		// keyGen.initialize(2048, SecureRandom.getInstanceStrong());
		keyGen.initialize(KEY_SIZE);
		KeyPair keypair = keyGen.generateKeyPair();
		// PrivateKey privKey = keypair.getPrivate();
		// PublicKey pubKey = keypair.getPublic();
		return keypair;
	}

	
	
	
	
	
	
	//LEIAME .............................................................................................
	//TODO rever isso ... qdo/como sera criado. 
	// Provavelmente sera um keystore pre-gerado e que ja vai por default com a aplicacao
	// para poder assinar os certificados criados (dos usuarios) ... garantindo q todos os usuarios 
	// tenham o certificado assinado pela mesma "CA" (o certificado default).
	// O keystore padrao sera tanto truststore (tem q adicionar os certificados padrao do cacerts, do java)
	// quanto keystore de clientes. Isso so para facilitar a implementacao, mas podemos deixar separado 
	// para facilitar a organizacao (mas aumenta a complexidade da impl)
	/*public void createDefaultKeyStore() throws Exception {
		System.out.println("Creating default keystore ...");
		KeyPair keyPair = createKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		Certificate trustCert = createCertificate(ROOT_DN, ROOT_DN, publicKey, privateKey);
		//Certificate[] outChain = { createCertificate("CN=xxxxx@xxx.xx", DEFAULT_DN, publicKey, privateKey), trustCert };
		Certificate[] outChain = { trustCert };

		System.out.println("Writing keystore ...");
		KeyStore outStore = KeyStore.getInstance(KEYSTORE_TYPE, PROVIDER);
		outStore.load(null, KEYSTORE_PASSWORD.toCharArray());
		outStore.setKeyEntry(ROOT_ALIAS, privateKey, KEYSTORE_PASSWORD.toCharArray(), outChain);
		OutputStream outputStream = new FileOutputStream(KEYSTORE_FILE);
		outStore.store(outputStream, KEYSTORE_PASSWORD.toCharArray());
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
		//TODO o issuer deve ser o do certificado padrao (nossa CA)
		//e ver se esta realmente assinando o novo certificado
		certGenerator.setIssuerDN(new X509Name(issuer)); // Set issuer!
		certGenerator.setNotBefore(Calendar.getInstance().getTime());
		certGenerator.setNotAfter(Calendar.getInstance().getTime());
		certGenerator.setPublicKey(publicKey);
		//certGenerator.setSignatureAlgorithm("SHA1withRSA");
		certGenerator.setSignatureAlgorithm("SHA1WithRSAEncryption");
		
		X509Certificate certificate = (X509Certificate) certGenerator.generate(privateKey, PROVIDER);
		System.out.println("Certificate created!");
		return certificate;
	}*/
/*
	
	private KeyStore loadDefaultCacerts() throws Exception{
		KeyStore caCerts = KeyStore.getInstance("JKS");
        String javaHome = System.getProperty("java.home");
        caCerts.load(
                new FileInputStream(javaHome + "/lib/security/cacerts"),
                "changeit".toCharArray());
        return caCerts;
	}*/
}
