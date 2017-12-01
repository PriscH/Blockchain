package com.prisch.transactions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class TransactionHandler extends StompSessionHandlerAdapter {

    @Autowired private TransactionRepository transactionRepository;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Transaction.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Transaction transaction = (Transaction)payload;
        transactionRepository.addPendingTransaction(transaction);

        System.out.println("\n\n Received a new transaction with ID " + transaction.getHash() + ".\n");
    }
}
