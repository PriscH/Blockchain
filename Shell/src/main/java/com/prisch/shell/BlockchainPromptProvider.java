package com.prisch.shell;

import com.prisch.messages.LocalMessages;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BlockchainPromptProvider implements PromptProvider {

    @Autowired private LocalMessages localMessages;

    @Override
    public AttributedString getPrompt() {
        if (localMessages.getUnreadMessageCount() == 0) {
            return new AttributedString("\nepicoin:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        }

        String unreadMessageText = String.format("You have %d unread message(s). You can display them with the print-messages command.", localMessages.getUnreadMessageCount());
        return new AttributedString("\n" + unreadMessageText + "\nepicoin:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
