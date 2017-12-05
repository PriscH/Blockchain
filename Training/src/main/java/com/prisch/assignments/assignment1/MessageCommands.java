package com.prisch.assignments.assignment1;

import com.prisch.ignore.StompSessionHolder;
import com.prisch.assignments.Settings;
import com.prisch.reference.services.HashService;
import com.prisch.reference.services.KeyService;
import com.prisch.ignore.shell.ShellLineReader;
import com.prisch.reference.messages.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Messaging")
public class MessageCommands {

    private static final Logger LOG = LoggerFactory.getLogger(MessageCommands.class);

    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private ShellLineReader shellLineReader;
    @Autowired private HashService hashService;
    @Autowired private KeyService keyService;

    @ShellMethod("Post a message to the network.")
    public void postMessage() {
        String content = shellLineReader.readLine("Type your message:\n");

        UserMessage message = new UserMessage();
        message.setAuthor(Settings.NAME);
        message.setContent(content);
        // TODO: [1A]
        // Use the hashService and keyService to complete the UserMessage
        // by providing a hash and signature of the content as well as your public key for verification

        LOG.info(message.toJson());
        stompSessionHolder.getStompSession().send("/app/postMessage", message);
    }

    private Availability postMessageAvailability() {
        if (!stompSessionHolder.isConnected()) {
            return Availability.unavailable("your client is not connected to the epicoin network (use 'connect' to connect).");
        }

        if (!keyService.checkKeysExist()) {
            return Availability.unavailable("you do not have a key pair yet (use 'generate-keys' to generate them).");
        }

        if (Settings.VERSION > 1) {
            return Availability.unavailable("sending messages is no longer available.");
        }

        return Availability.available();
    }
}
