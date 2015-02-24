package br.unb.cic.iris.mail.pgp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import net.suberic.crypto.EncryptionKeyManager;
import net.suberic.crypto.EncryptionManager;
import net.suberic.crypto.EncryptionUtils;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;

import br.unb.cic.iris.core.Configuration;
import br.unb.cic.iris.core.exception.EmailException;

//TODO tem q ver se ainda precisa instalar isso:
//Download http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
//Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download

public class PgpKeyManager {
	public static final String CONFIG_FILE_PRIVATE = "gpg.file.private";
	public static final String CONFIG_FILE_PUBLIC = "gpg.file.public";
	public static final String CONFIG_FILE_SECRET = "gpg.file.secret";
	/*TODO ADD no account.properties:
	gpg.file.private=/home/pedro/.gnupg/secring.gpg
	gpg.file.public=/home/pedro/.gnupg/pubring.gpg
	gpg.file.secret=12345678
	*/
	
	static final String PROVIDER = "BC";
	static final int KEY_SIZE = 2048;

	private String PRIVATE_FILE;
	private String PUBLIC_FILE;
	private char[] FILE_SECRET;
	private EncryptionUtils pgpUtils;

	static {
		// Security.addProvider(new BouncyCastleProvider());
		Security.insertProviderAt(new BouncyCastleProvider(), 0);
		System.out.println("Adding Bouncy Castle Provider ...");
	}
	
	public PgpKeyManager() throws FileNotFoundException, IOException, NoSuchProviderException{
		Properties props = new Properties();
		props.load(new FileInputStream(new File(Configuration.accountPropertyFile())));
		PRIVATE_FILE = props.getProperty(CONFIG_FILE_PRIVATE);
		PUBLIC_FILE = props.getProperty(CONFIG_FILE_PUBLIC);
		System.out.println("Public keystore: "+PUBLIC_FILE);
		System.out.println("Private keystore: "+PRIVATE_FILE);
		FILE_SECRET = props.getProperty(CONFIG_FILE_SECRET).toCharArray();
		pgpUtils = getEncryptionUtils();
	}

	public Key getPrivateKey(String id) throws Exception {
		EncryptionKeyManager pgpKeyMgr = pgpUtils.createKeyManager();
		pgpKeyMgr.loadPrivateKeystore(new FileInputStream(new File(PRIVATE_FILE)), FILE_SECRET);
		Iterator it = pgpKeyMgr.privateKeyAliases().iterator();
		while (it.hasNext()) {
			String alias = it.next().toString();
			if (alias.contains(id)) {
				return pgpKeyMgr.getPrivateKey(alias, FILE_SECRET);
			}
		}
		throw new EmailException("Couldn't find private key for: " + id + ". Please, generate the key pair");
	}

	public Key getPublicKey(String email) throws Exception {
		String publicKeyAlias = getLocalAlias(email);
		System.out.println("publicKeyAlias: " + publicKeyAlias);
		EncryptionKeyManager pgpKeyMgr = pgpUtils.createKeyManager();
		pgpKeyMgr.loadPublicKeystore(new FileInputStream(new File(PUBLIC_FILE)), FILE_SECRET);
		return pgpKeyMgr.getPublicKey(publicKeyAlias);
	}

	// o publicKeyAlias eh o email da pessoa.
	// O alias dentro do arquivo eh composto de ID NOME COMENTARIO EMAIL
	// Entao esse metodo pega o alias real (dentro arquivo) que contenha o email passado como param
	// esse alias "real" sera usado para recuperar a chave publica
	public String getLocalAlias(String email) throws Exception {
		EncryptionKeyManager pgpKeyMgr = pgpUtils.createKeyManager();
		pgpKeyMgr.loadPublicKeystore(new FileInputStream(new File(PUBLIC_FILE)), FILE_SECRET);
		Iterator it = pgpKeyMgr.publicKeyAliases().iterator();
		while (it.hasNext()) {
			String alias = it.next().toString();
			if (alias.contains(email)) {
				return alias;
			}
		}
		throw new EmailException("Couldn't find public key for: " + email + ". Please, install the public key.");
	}

	private EncryptionUtils getEncryptionUtils() throws NoSuchProviderException {
		return EncryptionManager.getEncryptionUtils(EncryptionManager.PGP);
	}
	
	

	
	
	// get remote public key, from mit server
	/*
	 * TODO public void retrievePublicKey(String id) {
	 * 
	 * }
	 */
	
	// TODO manter metodos para geracao de chaves?
	private PGPKeyRingGenerator generateKeyRingGenerator(String id, char[] pass) throws Exception {
		return generateKeyRingGenerator(id, pass, 0xc0);
	}
	public boolean existKeys() {
		return new File(PRIVATE_FILE).exists() && new File(PUBLIC_FILE).exists();
	}

	public void generateKeys(String id) throws Exception {
		// System.out.println("Generating keys for: " + id);
		generateKeys(id, PUBLIC_FILE, PRIVATE_FILE, FILE_SECRET);
	}

	private void generateKeys(String id, String pubFile, String secFile, char[] pass) throws Exception {
		System.out.println("Generating keys for: " + id);
		PGPKeyRingGenerator krgen = generateKeyRingGenerator(id, pass);

		// Generate public key ring, dump to file.
		PGPPublicKeyRing pkr = null;
		if (new File(pubFile).exists()) {
			System.out.println("existe pubFile ...");
			pkr = new PGPPublicKeyRing(new FileInputStream(pubFile), new BcKeyFingerprintCalculator());
			Iterator iterator = pkr.getPublicKeys();
			while (iterator.hasNext()) {
				PGPPublicKey pub = (PGPPublicKey) iterator.next();
				System.out.println("---> " + Long.toHexString(pub.getKeyID()));
			}
		} else {
			pkr = krgen.generatePublicKeyRing();
		}
		BufferedOutputStream pubout = new BufferedOutputStream(new FileOutputStream(pubFile));
		pkr.encode(pubout);
		pubout.close();

		/*
		 * EncryptionUtils pgpUtils = EncryptionManager.getEncryptionUtils(EncryptionManager.PGP); EncryptionKeyManager
		 * pgpKeyMgr = pgpUtils.createKeyManager(); pgpKeyMgr.loadPublicKeystore(new FileInputStream(new
		 * File(PUBLIC_FILE)), FILE_SECRET); pgpKeyMgr.setPublicKeyEntry(id, pkr.getPublicKey().);
		 */

		// Generate private key, dump to file.
		PGPSecretKeyRing skr = null;
		if (new File(secFile).exists()) {
			System.out.println("existe secFile ..");
			skr = new PGPSecretKeyRing(new FileInputStream(secFile), new BcKeyFingerprintCalculator());
		} else {
			skr = krgen.generateSecretKeyRing();
		}
		BufferedOutputStream secout = new BufferedOutputStream(new FileOutputStream(secFile));
		skr.encode(secout);
		secout.close();
	}

	// Note: s2kcount is a number between 0 and 0xff that controls the
	// number of times to iterate the password hash before use. More
	// iterations are useful against offline attacks, as it takes more
	// time to check each password. The actual number of iterations is
	// rather complex, and also depends on the hash function in use.
	// Refer to Section 3.7.1.3 in rfc4880.txt. Bigger numbers give
	// you more iterations. As a rough rule of thumb, when using
	// SHA256 as the hashing function, 0x10 gives you about 64
	// iterations, 0x20 about 128, 0x30 about 256 and so on till 0xf0,
	// or about 1 million iterations. The maximum you can go to is
	// 0xff, or about 2 million iterations. I'll use 0xc0 as a
	// default -- about 130,000 iterations.
	private PGPKeyRingGenerator generateKeyRingGenerator(String id, char[] pass, int s2kcount) throws Exception {
		// This object generates individual key-pairs.
		RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

		// Boilerplate RSA parameters, no need to change anything
		// except for the RSA key-size (2048). You can use whatever
		// key-size makes sense for you -- 4096, etc.
		kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), KEY_SIZE, 12));

		// First create the master (signing) key with the generator.
		PGPKeyPair rsakp_sign = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());
		// Then an encryption subkey.
		PGPKeyPair rsakp_enc = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());

		// Add a self-signature on the id
		PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();

		// Add signed metadata on the signature.
		// 1) Declare its purpose
		signhashgen.setKeyFlags(false, KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER);
		// 2) Set preferences for secondary crypto algorithms to use
		// when sending messages to this key.
		signhashgen.setPreferredSymmetricAlgorithms(false, new int[] { SymmetricKeyAlgorithmTags.CAST5, SymmetricKeyAlgorithmTags.AES_256, SymmetricKeyAlgorithmTags.AES_192,
				SymmetricKeyAlgorithmTags.AES_128 });
		signhashgen.setPreferredHashAlgorithms(false, new int[] { HashAlgorithmTags.SHA1, HashAlgorithmTags.SHA256, HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA512,
				HashAlgorithmTags.SHA224, });
		// 3) Request senders add additional checksums to the
		// message (useful when verifying unsigned messages.)
		signhashgen.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);

		// Create a signature on the encryption subkey.
		PGPSignatureSubpacketGenerator enchashgen = new PGPSignatureSubpacketGenerator();
		// Add metadata to declare its purpose
		enchashgen.setKeyFlags(false, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);

		// Objects used to encrypt the secret key.
		PGPDigestCalculator sha1Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);
		PGPDigestCalculator sha256Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);

		// bcpg 1.48 exposes this API that includes s2kcount. Earlier
		// versions use a default of 0x60.
		PBESecretKeyEncryptor pske = (new BcPBESecretKeyEncryptorBuilder(PGPEncryptedData.CAST5, sha1Calc, s2kcount)).build(pass);

		// Finally, create the keyring itself. The constructor
		// takes parameters that allow it to generate the self
		// signature.
		PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, rsakp_sign, id, sha1Calc, signhashgen.generate(), null,
				new BcPGPContentSignerBuilder(rsakp_sign.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1), pske);

		// Add our encryption subkey, together with its signature.
		keyRingGen.addSubKey(rsakp_enc, enchashgen.generate(), null);
		return keyRingGen;
	}



	public static void main(String args[]) {
		/*
		 * PgpKeyManager keyManager = new PgpKeyManager();
		 * 
		 * try { //keyManager.generateKeys("br.unb.cic.iris@gmail.com");
		 * //keyManager.generateKeys("canarioc@gmail.com");
		 * 
		 * keyManager.listPublicAliases(); keyManager.listPrivateAliases();
		 * 
		 * 
		 * System.out.println("\n\n\n\nPUB: "+keyManager.getPublicKey("canarioc@gmail.com"));
		 * System.out.println("\n\nPUB: "+keyManager.getPublicKey("br.unb.cic.iris@gmail.com")); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
	}
}
