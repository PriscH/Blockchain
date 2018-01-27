package com.prisch.transactions;

import com.prisch.messages.LocalMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class TransactionHandler extends StompSessionHandlerAdapter {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private LocalMessages localMessages;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Transaction.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Transaction transaction = (Transaction)payload;
        transactionRepository.addPendingTransaction(transaction);
        localMessages.addMessage(String.format("Received a new pending transaction (%s)", transaction.getHash()));
    }
}
