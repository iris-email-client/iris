package br.unb.cic.iris.security;

import static br.unb.cic.iris.security.PgpManager.ALGORITMO_SIMETRICO;
import static br.unb.cic.iris.security.PgpManager.FILE_PUBLIC;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Security;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;

public class PgpManager {
	public static final char ENCODING = PGPLiteralDataGenerator.UTF8;
	public static final String PROVIDER = "BC";
	public static final String FILE_PRIVATE = "/home/pedro/tmp/pgp/secret.asc";
	public static final String FILE_PUBLIC = "/home/pedro/tmp/pgp/pub.asc";
	public static final char[] FILE_SECRET = "12345678".toCharArray();
	

	public static final int KEY_SIZE = 2048;

	public static final int ALGORITMO_HASH = HashAlgorithmTags.SHA1;
	public static final int ALGORITMO_SIMETRICO = PGPEncryptedData.CAST5;
	public static final int ALGORITMO_ASSIMETRICO = PGPPublicKey.RSA_GENERAL;
	public static final String ALG_ASSIMETRICO = "RSA";
	
	static{
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public static String encrypt(String text) throws Exception {
		return new String(ByteArrayHandler.encrypt(text.getBytes(), FILE_SECRET, FILE_PUBLIC, ALGORITMO_SIMETRICO, true));
	}
	
	public static String decrypt(String encryptedText) throws Exception {
		return new String(ByteArrayHandler.decrypt(encryptedText.getBytes(), FILE_SECRET));
	}
	
}
