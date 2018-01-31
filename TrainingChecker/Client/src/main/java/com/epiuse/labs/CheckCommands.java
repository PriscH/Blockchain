package com.epiuse.labs;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ShellComponent
@ShellCommandGroup("Checks")
public class CheckCommands {

    @Autowired private WebSocketStompClient stompClient;

    @ShellMethod("Perform all checks.")
    public String checkAll() {
        return checkDependencies()
             + checkFileAccess()
             + checkConnection();
    }

    @ShellMethod("Checks whether all dependencies are available.")
    public String checkDependencies() {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            MessageDigest.getInstance("SHA-256");
            stringBuilder.append(buildCheckString(true, "SHA-256 loaded correctly.\n"));
        } catch (NoSuchAlgorithmException e) {
            stringBuilder.append(buildCheckString(false, "SHA-256 failed to load: " + e.getMessage() + "\n"));
        }

        try {
            KeyFactory.getInstance("RSA");
            stringBuilder.append(buildCheckString(true, "RSA loaded correctly.\n"));
        } catch (NoSuchAlgorithmException e) {
            stringBuilder.append(buildCheckString(false, "RSA failed to load: " + e.getMessage() + "\n"));
        }

        try {
            Signature.getInstance("SHA1WithRSA");
            stringBuilder.append(buildCheckString(true, "SHA1WithRSA loaded correctly.\n"));
        } catch (NoSuchAlgorithmException e) {
            stringBuilder.append(buildCheckString(false, "SHA1WithRSA failed to load: " + e.getMessage() + "\n"));
        }

        return stringBuilder.toString();
    }

    @ShellMethod("Checks that files can be accessed.")
    public String checkFileAccess() {
        final Path filePath = Paths.get("testfile.tmp");
        final String fileContent = "Hello World";

        StringBuilder stringBuilder = new StringBuilder();

        try {
            Files.write(filePath, Base64.getEncoder().encode(fileContent.getBytes()));
            stringBuilder.append(buildCheckString(true, "Message written to file.\n"));
        } catch (IOException e) {
            stringBuilder.append(buildCheckString(false, "Unable to write to file: " + e.getMessage() + "\n"));
        }

        try {
            String readContent = new String(Base64.getDecoder().decode((Files.readAllBytes(filePath))));
            if (readContent.equals(fileContent)) {
                stringBuilder.append(buildCheckString(true, "Message read from file.\n"));
            } else {
                stringBuilder.append(buildCheckString(false, "Message corrupted during IO.\n"));
            }
        } catch (IOException e) {
            stringBuilder.append(buildCheckString(false, "Unable to read from file: " + e.getMessage() + "\n"));
        }

        return stringBuilder.toString();
    }

    @ShellMethod("Checks that websocket connections are working.")
    public String checkConnection() {
        StringBuilder stringBuilder = new StringBuilder();

        ListenableFuture<StompSession> stompSessionFuture = stompClient.connect(Settings.SERVER_URL, new StompSessionHandlerAdapter() {});
        StompSession stompSession;
        try {
            stompSession = stompSessionFuture.get(Settings.TIMEOUT, TimeUnit.MILLISECONDS);
            stringBuilder.append(buildCheckString(true, "Connected to the server.\n"));
            stompSession.disconnect();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            stringBuilder.append(buildCheckString(false, "Unable to connect to the server: " + e.getMessage() + "\n"));
        }

        return stringBuilder.toString();
    }

    private String buildCheckString(boolean succeeded, String message) {
        AttributedStringBuilder stringBuilder = new AttributedStringBuilder().append("[");

        if (succeeded) {
            stringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                         .append("Pass");
        } else {
            stringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                         .append("Fail");
        }

        return stringBuilder.style(AttributedStyle.DEFAULT)
                            .append("] ")
                            .append(message)
                            .toAnsi();
    }
}
