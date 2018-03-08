package de.gamechest.backend.mail.client;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by ByteList on 08.03.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class MailClient {

    private final String fromAddress;
    private final Properties properties;
    private final Session session;

    public MailClient(String sender, String host, String user, String password) {
        this.fromAddress = sender;
        this.properties = System.getProperties();
        this.properties.setProperty("mail.smtp.host", host);
        this.properties.setProperty("mail.smtp.auth", "true");
        this.session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
    }

    public void sendTestMail() {
        sendMail("Test Mail yo!", "hoffe das kommt an höhö", "temp@bytelist.de");
    }

    private void sendMail(String subject, String text, String toAddress) {
        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(fromAddress));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));

            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);
            System.out.println("[Mail] Sent mail to "+toAddress+": "+subject);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }
}
