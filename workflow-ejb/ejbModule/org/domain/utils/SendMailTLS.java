package org.domain.utils;
 
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
 
@Name("sendMail")
public class SendMailTLS {
	
	@In(value = "constantsBuilder", create = true) ConstantsBuilder pathBuilder;
 
	public void sendMail(String to, String name, String subject, String msg) throws MessagingException {
	//public static void main(String[] args) {
 
		final String toEmail = to;
		
		final String email = pathBuilder.getMailSender();
		final String password = pathBuilder.getMailSenderPwd();
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
 		
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email, password);
			}
		  });
 
		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(toEmail));
			message.setSubject(subject);
			message.setText("Dear "+name+","
				+ "\n\n" + msg);
 
			Transport.send(message);

	}
}