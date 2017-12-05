package com.prisch.ignore.transactions;

import com.prisch.assignments.assignment6.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class TransactionSyncHandler extends StompSessionHandlerAdapter {

    @Autowired private TransactionRepository transactionRepository;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return TransactionSync.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        TransactionSync transactionSync = (TransactionSync)payload;
        transactionRepository.syncPendingTransactions(transactionSync);
    }
}
