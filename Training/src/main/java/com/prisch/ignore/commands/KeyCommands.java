package com.prisch.ignore.commands;

import com.prisch.reference.Constants;
import com.prisch.reference.services.KeyService;
import com.prisch.ignore.shell.ShellLineReader;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@ShellComponent
@ShellCommandGroup("Wallet")
public class KeyCommands {

    @Autowired private ShellLineReader shellLineReader;
    @Autowired private KeyService keyService;

    @ShellMethod("Generate Public-Private key pair")
    public String generateKeys() throws Exception {
        final String WARNING_MESSAGE = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                                                    .append("WARNING: ")
                                                                    .style(AttributedStyle.DEFAULT)
                                                                    .append("This will overwrite any existing key pair you might have. ")
                                                                    .append("This action is ")
                                                                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                                                                    .append("non-reversible")
                                                                    .style(AttributedStyle.DEFAULT)
                                                                    .append(".\n")
                                                                    .append("Confirm by typing 'yes' or press enter to cancel: ")
                                                                    .toAnsi();

        String confirmation = shellLineReader.readLine(WARNING_MESSAGE);
        if (confirmation.equalsIgnoreCase("yes")) {
            writeKeys();
            keyService.resetAddress();
            return "Successfully generated a key pair. You have a new address.";
        }
        return "Key pair generation cancelled.";
    }

    private void writeKeys() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(512);
        KeyPair keyPair = keyGenerator.generateKeyPair();

        Files.write(Constants.PUBLIC_KEY_PATH, Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
        Files.write(Constants.PRIVATE_KEY_PATH, Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()));
    }
}
