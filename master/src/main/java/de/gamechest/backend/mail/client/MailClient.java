package de.gamechest.backend.mail.client;

import com.google.common.io.Resources;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
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
        try {
            String content = Resources.toString(Resources.getResource("mails/register.html"), Charset.forName("UTF-8"))
                    .replace("#{user.name}", "ByteList").replace("#{user.verifyUrl}", "https://game-chest.de/register.php?mail=bla");
            sendHtmlMail("Registrierung auf game-chest.de", content, "mail@bytelist.de");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendRegisterMail(String mail, String user, String verifyCode) {
        try {
            String content = Resources.toString(Resources.getResource("mails/register.html"), Charset.forName("UTF-8"))
                    .replace("#{user.name}", user).replace("#{user.verifyUrl}", "https://game-chest.de/register/"+verifyCode+"/");
            return sendHtmlMail("Registrierung auf game-chest.de", content, mail);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendMail(String subject, String text, String toAddress) {
        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(fromAddress));
            message.setSender(new InternetAddress(fromAddress));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));

            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);
            System.out.println("[Mail] Sent mail to "+toAddress+": "+subject);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    private boolean sendHtmlMail(String subject, Object content, String toAddress) {
        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(fromAddress, "GameChest"));
            message.setSender(new InternetAddress("game-chest.de"));
            message.setSentDate(new Date());

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));

            message.setSubject(subject);
            message.setContent(content, "text/html");

            Transport.send(message);
            System.out.println("[Mail] Sent mail to "+toAddress+": "+subject);
            return true;
        } catch (MessagingException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
