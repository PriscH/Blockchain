package com.prisch.commands;

import com.prisch.global.Constants;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ShellComponent
@ShellCommandGroup("Address and Keys")
public class PrintAddressCommand {

    @ShellMethod("Print your epicoin address based on your public key.")
    public String printAddress() throws Exception {
        if (!Files.exists(Constants.PUBLIC_KEY_PATH)) {
            return new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                                                                      .append("ERROR: ")
                                                                      .style(AttributedStyle.DEFAULT)
                                                                      .append("You need a key pair in order to retrieve the corresponding address. ")
                                                                      .append("Use the generate-keys command to create a key pair first.")
                                                                      .toAnsi();
        }

        String publicKey = readPublicKey();
        String publicKeyHash = hashPublicKey(publicKey);
        return generateAddress(publicKeyHash);
    }

    private String readPublicKey() throws IOException {
        byte[] publicKeyStream = Files.readAllBytes(Constants.PUBLIC_KEY_PATH);
        return DatatypeConverter.printHexBinary(publicKeyStream);
    }

    private String hashPublicKey(String publicKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] publicKeyHash = digest.digest(publicKey.getBytes());
        return DatatypeConverter.printHexBinary(publicKeyHash);
    }

    private String generateAddress(String publicKeyHash) {
        return publicKeyHash.substring(0, Constants.ADDRESS_LENGTH);
    }
}
