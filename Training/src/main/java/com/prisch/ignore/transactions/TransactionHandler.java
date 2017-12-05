package com.prisch.ignore.transactions;

import com.prisch.assignments.assignment6.TransactionRepository;
import com.prisch.ignore.messages.MessageHolder;
import com.prisch.reference.transactions.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class TransactionHandler extends StompSessionHandlerAdapter {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private MessageHolder messageHolder;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Transaction.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Transaction transaction = (Transaction)payload;
        transactionRepository.addPendingTransaction(transaction);
        messageHolder.addMessage(String.format("Received a new pending transaction (%s)", transaction.getHash()));
    }
}
