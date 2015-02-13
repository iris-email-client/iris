package br.unb.cic.iris.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Security;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.w3c.tidy.Tidy;




public class KeyServer {
	private final String USER_AGENT = "Mozilla/5.0";
	//http://pgp.mit.edu/pks/add
	private final static String BASE_URL = "http://pgp.mit.edu";
	private final static String URL_SUBMIT = BASE_URL + "/pks/add/";
	private final static String URL_LOOKUP = BASE_URL + "/pks/lookup?op=get&search=";

	// HTTP POST request
	private void sendKey(String key) throws Exception {
		URL obj = new URL(URL_SUBMIT);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "keytext=" + key;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + URL_SUBMIT);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println("Response: ");
		System.out.println(response.toString());

	}
	
	public void send2(String key) throws Exception {
		String rawData = "keytext=" + key;
		String type = "application/x-www-form-urlencoded";
		String encodedData = URLEncoder.encode( rawData , "UTF-8"); 
		URL u = new URL(URL_SUBMIT);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		conn.setRequestProperty( "Content-Type", type );
		conn.setRequestProperty( "Content-Length", String.valueOf(encodedData.length()));
		OutputStream os = conn.getOutputStream();
		os.write(encodedData.getBytes());
		os.flush();
		os.close();
		//DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		//wr.writeBytes(encodedData);
		//wr.flush();
		//wr.close();
		
		
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + URL_SUBMIT);
		//System.out.println("Post parameters : " + rawData);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println("Response: ");
		System.out.println(response.toString());
	}

	
	private void send3(String key) throws Exception {
		String url = URL_SUBMIT;
		 
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
	 
		// add header
		post.setHeader("User-Agent", USER_AGENT);
	 
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("keytext", key));
	 
		post.setEntity(new UrlEncodedFormEntity(urlParameters));
	 
		HttpResponse response = client.execute(post);
		System.out.println("Response Code : " 
	                + response.getStatusLine().getStatusCode());
		System.out.println(response.toString());
		System.out.println(response.getStatusLine());
	 
		BufferedReader rd = new BufferedReader(
		        new InputStreamReader(response.getEntity().getContent()));
	 
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
	}
	
	public String get(String identity) throws Exception{
		//http://pgp.mit.edu/pks/lookup?search=teste_iris%40gmail.com&op=get
		String p = identity.replaceAll("@", "%40");
		System.out.println("P="+p);
		String url = URL_LOOKUP+p;
		 
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
	 
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		HttpResponse response = client.execute(request);
	 
		System.out.println("Response Code : " 
	                + response.getStatusLine().getStatusCode());
	 
		/*BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));
	 
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		System.out.println("result="+result.toString());*/
		
		
		
		Tidy tidy = new Tidy(); // obtain a new Tidy instance
		tidy.setXHTML(false); // set desired config options using tidy setters 
		                           // (equivalent to command line options)
		StringWriter outputWriter = new StringWriter();

		tidy.parse(response.getEntity().getContent(), outputWriter); // run tidy, providing an input and output stream

		System.out.println("jtidy.....");
		System.out.println(outputWriter.toString());
		
		
		String tmp = outputWriter.toString();
		tmp = tmp.substring(tmp.indexOf("<pre>")+5, tmp.indexOf("</pre>"));
		System.out.println("\n\n\nTMP=\n"+tmp);
		return tmp;
	}
	
	private static String readFile(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		// delete the last ls
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		return stringBuilder.toString();
	}
	
	

	public static void main(String[] args) {
		String filePrivate = "/home/pedro/tmp/pgp/tmpPrivate.asc";
		String filePublic = "/home/pedro/tmp/pgp/tmpPublic.asc";
		String identity = "teste_iris2@gmail.com";
		char[] pass = "12345678".toCharArray();

		PgpKeyGenerator keyGenerator = new PgpKeyGenerator();
		KeyServer server = new KeyServer();
		try {
			// gera as chaves em arquivo
			keyGenerator.generateKeys(identity, pass, filePrivate, filePublic);
			// le a chave publica do arquivo
			String key = readFile(filePublic);
			System.out.println("KEY=\n"+key);
			server.teste(filePublic);
			// envia a chave para o servidor
			//server.sendKey(key);
			//server.send2(key);
//			/server.send3(key);
			String pubKeyStr = server.get("teste_iris@gmail.com");
			System.out.println("chave recuperada="+pubKeyStr);
			
			/*String tmpFile = "/home/pedro/tmp/pgp/chaveRecuperada.asc";
			server.write(tmpFile, pubKeyStr);
			PGPPublicKey publicKey = PGPExampleUtil.readPublicKey(tmpFile);
			System.out.println("chave="+publicKey);*/
			
			PGPPublicKey publicKey = server.read(pubKeyStr);
			System.out.println("chave="+publicKey);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public PGPPublicKey read(String str) throws Exception{
		InputStream in=new ByteArrayInputStream(str.getBytes());
	    in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(in);

	    JcaPGPPublicKeyRingCollection pgpPub = new JcaPGPPublicKeyRingCollection(in);
	    in.close();

	    PGPPublicKey key = null;
	    Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
	    System.out.println("hasNext: "+rIt.hasNext());
	    while (key == null && rIt.hasNext())
	    {
	        PGPPublicKeyRing kRing = rIt.next();
	        Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
	        while (key == null && kIt.hasNext())
	        {
	            PGPPublicKey k = kIt.next();
	            System.out.println("K="+k);
	            if (k.isEncryptionKey())
	            {
	                key = k;
	            }
	        }
	    }
	    return key;
	}
	
	public void teste(String pub) throws Exception{
		// add the bouncy castle security provider
		// or have it installed in $JAVA_HOME/jre/lib/ext
		Security.addProvider(new BouncyCastleProvider());
		 
		// read a public key from a file
		PGPPublicKeyRing keyRing = getKeyring(new FileInputStream(new File(pub)));

		// read a public key from that keyring
		PGPPublicKey publicKey = getEncryptionKey(keyRing);
		 
		System.out.println("Public Key: " + publicKey);
		System.out.println(" ID: " + publicKey.getKeyID());
		System.out.println(" ID(hex): "+Long.toHexString(publicKey.getKeyID()).toUpperCase());
		System.out.println(" FINGERPRINT: "+publicKey.getFingerprint());

	}
	/**
	 * Decode a PGP public key block and return the keyring it represents.
	 */
	public PGPPublicKeyRing getKeyring(InputStream keyBlockStream) throws IOException {
	    // PGPUtil.getDecoderStream() will detect ASCII-armor automatically and decode it,
	    // the PGPObject factory then knows how to read all the data in the encoded stream
	    PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(keyBlockStream));
	 
	    // these files should really just have one object in them,
	    // and that object should be a PGPPublicKeyRing.
	    Object o = factory.nextObject();
	    if (o instanceof PGPPublicKeyRing) {
	        return (PGPPublicKeyRing)o;
	    }
	    throw new IllegalArgumentException("Input text does not contain a PGP Public Key");
	}
	 
	/**
	 * Get the first encyption key off the given keyring.
	 */
	public PGPPublicKey getEncryptionKey(PGPPublicKeyRing keyRing) {
	    if (keyRing == null)
	        return null;
	 
	    // iterate over the keys on the ring, look for one
	    // which is suitable for encryption.
	    Iterator keys = keyRing.getPublicKeys();
	    PGPPublicKey key = null;
	    while (keys.hasNext()) {
	        key = (PGPPublicKey)keys.next();
	        if (key.isEncryptionKey()) {
	            return key;
	        }
	    }
	    return null;
	}
	
	
	
    private void write(String filePath, String content) throws Exception{
    	File file = new File(filePath);
		//String content = "This is the text content";
 
		try (FileOutputStream fop = new FileOutputStream(file)) {
 
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			// get the content in bytes
			byte[] contentInBytes = content.getBytes();
 
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
