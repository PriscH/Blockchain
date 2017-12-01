package com.prisch.commands;

import com.prisch.global.Constants;
import com.prisch.services.KeyService;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@ShellComponent
@ShellCommandGroup("Wallet")
public class PrintAddressCommand {

    @Autowired private KeyService keyService;

    @ShellMethod("Print your epicoin address based on your public key.")
    public String printAddress() throws Exception {
        if (!Files.exists(Constants.PUBLIC_KEY_PATH)) {
            return new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                                                                      .append("ERROR: ")
                                                                      .style(AttributedStyle.DEFAULT)
                                                                      .append("You need a key pair in order to retrieve the corresponding address. ")
                                                                      .append("Use the ")
                                                                      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                                                      .append("generate-keys ")
                                                                      .style(AttributedStyle.DEFAULT)
                                                                      .append("command to create a key pair first.")
                                                                      .toAnsi();
        }

        String publicKey = keyService.readPublicKey();
        String publicKeyHash = hashPublicKey(publicKey);
        return generateAddress(publicKeyHash);
    }

    private String hashPublicKey(String publicKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] publicKeyHash = digest.digest(publicKey.getBytes());
        return Base64.getEncoder().encodeToString(publicKeyHash);
    }

    private String generateAddress(String publicKeyHash) {
        return publicKeyHash.substring(0, Constants.ADDRESS_LENGTH);
    }
}
