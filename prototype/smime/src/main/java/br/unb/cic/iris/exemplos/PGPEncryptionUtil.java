package br.unb.cic.iris.exemplos;

import static br.unb.cic.iris.security.PgpManager.*;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
 
public class PGPEncryptionUtil {
 
    // pick some sensible encryption buffer size
    private static final int BUFFER_SIZE = 4096;
 
    // encrypt the payload data using AES-256,
    // remember that PGP uses a symmetric key to encrypt
    // data and uses the public key to encrypt the symmetric
    // key used on the payload.
    //private static final int PAYLOAD_ENCRYPTION_ALG = PGPEncryptedData.AES_256;
 
    // various streams we're taking care of
    private final ArmoredOutputStream armoredOutputStream;
    private final OutputStream encryptedOut;
    private final OutputStream compressedOut;
    private final OutputStream literalOut;
 
    public PGPEncryptionUtil(PGPPublicKey key, String payloadFilename, OutputStream out) throws PGPException, NoSuchProviderException, IOException {
 
        // write data out using "ascii-armor" encoding.  This is the
        // normal PGP text output.
        this.armoredOutputStream = new ArmoredOutputStream(out);
 
        // create an encrypted payload and set the public key on the data generator
        PGPEncryptedDataGenerator encryptGen = null;//new PGPEncryptedDataGenerator(PAYLOAD_ENCRYPTION_ALG, new SecureRandom(), BC_PROVIDER_NAME);
        //encryptGen.addMethod(key);
 
        byte[] buffer = new byte[1024];
		// open an output stream connected to the encrypted data generator
        // and have the generator write its data out to the ascii-encoding stream
        this.encryptedOut = encryptGen.open(armoredOutputStream, buffer );
 
        // compress data.  we are building layers of output streams.  we want to compress here
        // because this is "before" encryption, and you get far better compression on
        // unencrypted data.
        PGPCompressedDataGenerator compressor = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
        this.compressedOut = compressor.open(encryptedOut);
 
        // now we have a stream connected to a data compressor, which is connected to
        // a data encryptor, which is connected to an ascii-encoder.
        // into that we want to write a PGP "literal" object, which is just a named
        // piece of data (as opposed to a specially-formatted key, signature, etc)
        PGPLiteralDataGenerator literalGen = new PGPLiteralDataGenerator();
        this.literalOut = literalGen.open(compressedOut, ENCODING,
                                          payloadFilename, new Date(), new byte[BUFFER_SIZE]);
    }
 
    /**
     * Get an output stream connected to the encrypted file payload.
     */
    public OutputStream getPayloadOutputStream() {
        return this.literalOut;
    }
 
    /**
     * Close the encrypted output writers.
     */
    public void close() throws IOException {
        // close the literal output
        literalOut.close();
 
        // close the compressor
        compressedOut.close();
 
        // close the encrypted output
        encryptedOut.close();
 
        // close the armored output
        armoredOutputStream.close();
    }
}
