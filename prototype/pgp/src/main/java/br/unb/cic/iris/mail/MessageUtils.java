package br.unb.cic.iris.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

//NOVO
public class MessageUtils {

	public static String getMimeType(String contentType) {
		String str = "unknown";
		StringTokenizer st = new StringTokenizer(contentType.trim(), ";");
		if (st.hasMoreTokens()) {
			str = st.nextToken();
			// str should look like:
			// text/plain, text/html, multipart/mixed, multipart/alternative,
			// multipart/related, or application/octec-stream, etc.
		}

		return str.toLowerCase();
	}

	public static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
}
