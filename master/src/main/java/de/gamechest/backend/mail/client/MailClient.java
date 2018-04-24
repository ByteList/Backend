package de.gamechest.backend.mail.client;

import com.google.common.io.Resources;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by ByteList on 08.03.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class MailClient {

    private final File privateKeyData;
    private final Mailer mailer;
    private final String fromAddress;

    public MailClient(File privateKeyData, String host, String user, String password) {
        this.privateKeyData = privateKeyData;
        this.mailer = MailerBuilder
                .withSMTPServer(host, 587, user, password)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();
        this.fromAddress = user;
    }

    public boolean sendRegisterMail(String mail, String user, String verifyCode) {
        try {
            String content = Resources.toString(Resources.getResource("mails/register.html"), Charset.forName("UTF-8"))
                    .replace("#{user.name}", user).replace("#{user.verifyUrl}", "https://game-chest.de/register/"+verifyCode+"/");
            return sendMail(mail, user, "Registrierung auf game-chest.de", content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean sendMail(String mail, String user, String subject, String html) {
        try {
            Email email = EmailBuilder.startingBlank()
                    .to(user, mail)
                    .from("GameChest", new InternetAddress(fromAddress))
                    .withReplyTo("GameChest", "gamechestmc@gmail.com")
                    .withSubject(subject)
                    .withHTMLText(html)
                    .signWithDomainKey(privateKeyData, "bytelist.de", "apr2018.bytelist")
                    .buildEmail();
            this.mailer.sendMail(email);
            return true;
        } catch (AddressException e) {
            e.printStackTrace();
        }
        return false;
    }
}
