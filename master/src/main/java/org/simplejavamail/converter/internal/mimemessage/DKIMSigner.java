package org.simplejavamail.converter.internal.mimemessage;

import net.markenwerk.utils.mail.dkim.Canonicalization;
import net.markenwerk.utils.mail.dkim.DkimMessage;
import net.markenwerk.utils.mail.dkim.DkimSigner;
import net.markenwerk.utils.mail.dkim.SigningAlgorithm;
import org.simplejavamail.email.Email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by ByteList on 23.02.2019.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class DKIMSigner implements IDKIMSigner {
    public DKIMSigner() {
    }

    public MimeMessage signMessageWithDKIM(MimeMessage messageToSign, Email emailContainingSigningDetails) {
        try {
            DkimSigner dkimSigner;
            if (emailContainingSigningDetails.getDkimPrivateKeyFile() != null) {
                dkimSigner = new DkimSigner(emailContainingSigningDetails.getDkimSigningDomain(), emailContainingSigningDetails.getDkimSelector(), emailContainingSigningDetails.getDkimPrivateKeyFile());
            } else {
                dkimSigner = new DkimSigner(emailContainingSigningDetails.getDkimSigningDomain(), emailContainingSigningDetails.getDkimSelector(), emailContainingSigningDetails.getDkimPrivateKeyInputStream());
            }


            // To fix the org.simplejavamail.converter.internal.mimemessage.MimeMessageParseException:
            // "The domain part of noreply@game-chest.de has to be bytelist.de or a subdomain thereof"
            // I override this class to set the identity directly without checking using reflections.
//            dkimSigner.setIdentity(emailContainingSigningDetails.getFromRecipient().getAddress());
            setIdentity(emailContainingSigningDetails.getFromRecipient().getAddress().trim(), dkimSigner);

            dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
            dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
            dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA256_WITH_RSA);
            dkimSigner.setLengthParam(true);
            dkimSigner.setZParam(false);
            return new DkimMessage(messageToSign, dkimSigner);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | MessagingException | IOException | NoSuchFieldException | IllegalAccessException var4) {
            throw new MimeMessageParseException("Error signing MimeMessage with DKIM", var4);
        }
    }

    private void setIdentity(String address, DkimSigner dkimSigner) throws IllegalAccessException, NoSuchFieldException {
        if(address == null || dkimSigner == null) return;

        Field identity = dkimSigner.getClass().getDeclaredField("identity");
        identity.setAccessible(true);
        identity.set(dkimSigner, address);
    }
}
