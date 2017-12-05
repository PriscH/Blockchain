package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.global.Settings;
import com.prisch.messages.UserMessage;
import com.prisch.services.HashService;
import com.prisch.services.KeyService;
import com.prisch.shell.ShellLineReader;
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
        String content = shellLineReader.readLine("Type your message:");

        UserMessage message = new UserMessage();
        message.setAuthor(Settings.NAME);
        message.setContent(content);
        message.setHash(hashService.hash(content));
        message.setSignature(keyService.sign(content));
        message.setPublicKey(keyService.readPublicKey());

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

        return Availability.available();
    }
}
