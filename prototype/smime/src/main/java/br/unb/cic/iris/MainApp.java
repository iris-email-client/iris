package br.unb.cic.iris;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.unb.cic.iris.mail.EmailReader;
import br.unb.cic.iris.mail.EmailSender;
import br.unb.cic.iris.mail.EmailSenderSmime;

public class MainApp {
	public static final String username = "canarioc@gmail.com";
	public static final String password = "capoeirarte";
	
	public static final String TO = "br.unb.cic.iris@gmail.com";
	public static final String TO_PASS = "mitologia";
	
	public static void main(String[] args) {
		System.out.println("Executing ...");
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		//EmailSender sender = new EmailSender();
		EmailSender sender = new EmailSenderSmime();
		EmailReader reader = new EmailReader();
		
		int x = 3;
		try {
			for(int i=0; i < x; i++){
				String date = sdf.format(new Date());
				String subject = "encriptado e assinado - "+date;
				String body = "BODY ENCRYPTED AND SIGNED ... teste123 ... \n"+date;
				sender.sendMessage(TO, subject, body);
				System.out.println("\n");
				
				Thread.sleep(2000);
			}
			
			System.out.println("\n\n\n\nREADING .......................");
			reader.showMessages(TO, TO_PASS, "INBOX", x);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
