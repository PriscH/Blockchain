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
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@ShellComponent
@ShellCommandGroup("Address and Keys")
public class GenerateKeyCommand {

    @ShellMethod("Generate Public-Private key pair")
    public String generateKeys() throws Exception {
        final String WARNING_MESSAGE = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
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

        String confirmation = readLine(WARNING_MESSAGE);
        if (confirmation.equalsIgnoreCase("yes")) {
            writeKeys();
            return "Successfully generated a key pair. You have a new address.";
        }
        return "Key pair generation cancelled.";
    }

    private void writeKeys() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(512);
        KeyPair keyPair = keyGenerator.generateKeyPair();

        writeKeyToDisk(keyPair.getPublic(), Constants.PUBLIC_KEY_PATH);
        writeKeyToDisk(keyPair.getPrivate(), Constants.PRIVATE_KEY_PATH);
    }

    private void writeKeyToDisk(Key key, Path keyPath) throws IOException {
        String keyContent = DatatypeConverter.printHexBinary(key.getEncoded());
        Files.write(keyPath, keyContent.getBytes());
    }

    private String readLine(String message) {
        String response = System.console().readLine(message);
        System.out.println();
        System.out.println();
        return response;
    }
}
