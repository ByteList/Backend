package de.gamechest.backend.mail.client;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
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


    public MailClient() {
        this.fromAddress = "noreply@game-chest.de";
        this.properties = System.getProperties();
        this.properties.setProperty("mail.smtp.host", "localhost");
    }

    public void sendTestMail() {
        sendMail("Test Mail yo!", "hoffe das kommt an höhö", "temp@bytelist.de");
    }

    private void sendMail(String subject, String text, String toAddress) {
        Session session = Session.getDefaultInstance(properties);
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
