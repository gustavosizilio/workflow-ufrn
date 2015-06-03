package org.domain.utils;

import java.io.IOException;

public class MailGun {
	public static synchronized void sendMail(String to, String name, String subject, String msg) throws IOException, InterruptedException {
		System.out.println("SENDING MAIL");
		String cmd = "curl -s --user 'api:key-e436b7b9e4f0458448706df4244e9ac9' "
				+ "https://api.mailgun.net/v2/sandboxc0b684c5583b4c7e90e538440670fd0c.mailgun.org/messages "
				+ "-F from='Experiment Executer <postmaster@sandboxc0b684c5583b4c7e90e538440670fd0c.mailgun.org>' "
				+ "-F to='"+name+" <"+to+">' "
				+ "-F subject='"+subject+"' "
				+ "-F text='"+msg+"'";
		System.out.println(cmd);
		Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
	    p.waitFor();
	}
}
