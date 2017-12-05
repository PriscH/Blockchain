package com.prisch.assignments.assignment1;

import com.prisch.ignore.messages.MessageHolder;
import com.prisch.reference.messages.UserMessage;
import com.prisch.reference.services.HashService;
import com.prisch.reference.services.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class UserMessageHandler extends StompSessionHandlerAdapter {

    @Autowired private MessageHolder messageHolder;
    @Autowired private HashService hashService;
    @Autowired private KeyService keyService;
    @Autowired private SignatureVerifier signatureVerifier;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return UserMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        UserMessage userMessage = (UserMessage)payload;

        // TODO: [1B]
        // Use the hashService and keyService to verify that the received userMessage is consistent and signed
        // Modify the message provided to the messageHolder to indicate this
        messageHolder.addMessage(String.format("%s says '%s'", userMessage.getAuthor(), userMessage.getContent()));
    }
}