package br.unb.cic.iris.security;

public class MainApp {
	public static final String username = "xxx@gmail.com";
	public static final String password = "xxx";
	
	static final String TO = "xxx@gmail.com";
	
	
	public static void main(String[] args) {
		System.out.println("Executing ...");
		//EmailSender sender = new EmailSender();
		EmailSender sender = new EmailSenderSmime();
		
		String subject = "teste encriptado e assinado";
		String body = "teste 123";
		try {
			sender.sendMessage(TO, subject, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
