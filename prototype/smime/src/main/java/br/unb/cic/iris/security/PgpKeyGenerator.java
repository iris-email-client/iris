package br.unb.cic.iris.security;

import static br.unb.cic.iris.security.PgpManager.*;
import static br.unb.cic.iris.security.PgpManager.ALGORITMO_HASH;
import static br.unb.cic.iris.security.PgpManager.ALGORITMO_SIMETRICO;
import static br.unb.cic.iris.security.PgpManager.ALG_ASSIMETRICO;
import static br.unb.cic.iris.security.PgpManager.FILE_PUBLIC;
import static br.unb.cic.iris.security.PgpManager.FILE_PRIVATE;
import static br.unb.cic.iris.security.PgpManager.PROVIDER;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

public class PgpKeyGenerator {

	public void generateKeys(String identity, char[] passPhrase) throws Exception {
		FileOutputStream secret = new FileOutputStream(FILE_PRIVATE);
		FileOutputStream pub = new FileOutputStream(FILE_PUBLIC);

		Security.addProvider(new BouncyCastleProvider());

		KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALG_ASSIMETRICO, PROVIDER);

		kpg.initialize(KEY_SIZE);

		KeyPair kp = kpg.generateKeyPair();

		exportKeyPair(secret, pub, kp, identity, passPhrase, true);
	}

	private static void exportKeyPair(OutputStream secretOut, OutputStream publicOut, KeyPair pair, String identity, char[] passPhrase, boolean armor) throws Exception {
		if (armor) {
			secretOut = new ArmoredOutputStream(secretOut);
		}

		PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(ALGORITMO_HASH);
		PGPKeyPair keyPair = new JcaPGPKeyPair(ALGORITMO_ASSIMETRICO, pair, new Date());
		PGPSecretKey secretKey = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, keyPair, identity, sha1Calc, null, null, new JcaPGPContentSignerBuilder(keyPair
				.getPublicKey().getAlgorithm(), ALGORITMO_HASH), new JcePBESecretKeyEncryptorBuilder(ALGORITMO_SIMETRICO, sha1Calc).setProvider(PROVIDER).build(passPhrase));

		secretKey.encode(secretOut);

		secretOut.close();

		if (armor) {
			publicOut = new ArmoredOutputStream(publicOut);
		}

		PGPPublicKey key = secretKey.getPublicKey();

		key.encode(publicOut);

		publicOut.close();
	}

	public static void main(String[] args) throws Exception {
		String identity = "canarioc@gmail.com";
		String passPhrase = "12345678";
		new PgpKeyGenerator().generateKeys(identity, passPhrase.toCharArray());
	}
}
