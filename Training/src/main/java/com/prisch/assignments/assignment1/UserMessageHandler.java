package com.prisch.assignments.assignment1;

import com.prisch.ignore.messages.LocalMessages;
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

    @Autowired private LocalMessages localMessages;
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

        // TODO: [1C]
        // Use the hashService and keyService in conjunction with the SignatureVerifier to verify that the received userMessage is consistent and signed
        boolean hashValid = true;
        boolean signatureValid = true;

        if (!hashValid) {
            localMessages.addMessage(String.format("%s provided an invalid hash on their message (%s).", userMessage.getAuthor(), userMessage.getContent()));
        } else if (!signatureValid) {
            localMessages.addMessage(String.format("%s provided an invalid signature on their message (%s).", userMessage.getAuthor(), userMessage.getContent()));
        } else {
            localMessages.addMessage(String.format("%s says '%s' [VERIFIED]", userMessage.getAuthor(), userMessage.getContent()));
        }

        // TODO: [1+]
        // Use the provided public key to send the author a secret message
        // Encrypt your message so that only the author is able to decrypt it with their private key
        // Write corresponding code to accept incoming messages address to you and encrypted with your public key
        // and decrypt them
    }
}