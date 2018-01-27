package com.prisch.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class UserMessageHandler extends StompSessionHandlerAdapter {

    @Autowired private LocalMessages localMessages;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return UserMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        UserMessage userMessage = (UserMessage)payload;
        localMessages.addMessage(String.format("%s says '%s'", userMessage.getAuthor(), userMessage.getContent()));
    }
}