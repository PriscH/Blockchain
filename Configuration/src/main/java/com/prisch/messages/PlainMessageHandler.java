package com.prisch.messages;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class PlainMessageHandler extends StompSessionHandlerAdapter {

    @Autowired private MessageHolder messageHolder;

    @Override public Type getPayloadType(StompHeaders headers) {
        return PlainMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        PlainMessage plainMessage = (PlainMessage)payload;

        if (plainMessage.getType() == PlainMessage.Type.INFO) {
            messageHolder.addMessage(plainMessage.getContent());
        } else {
            StringBuilder messageBuilder = new StringBuilder();
            switch (plainMessage.getType()) {
                case ERROR:
                    messageBuilder.append(new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                                                                       .append("ERROR: ")
                                                                       .style(AttributedStyle.DEFAULT)
                                                                       .toAnsi());
                    break;
                case WARNING:
                    messageBuilder.append(new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                                                                       .append("WARNING: ")
                                                                       .style(AttributedStyle.DEFAULT)
                                                                       .toAnsi());
            }

            System.out.println("\n\n" + messageBuilder.toString() + plainMessage.getContent() + "\n");
        }
    }
}
