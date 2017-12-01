package com.prisch.transactions;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TransactionRepository {

    private final Map<String, Transaction> pendingTransactionMap = new ConcurrentHashMap<>();

    public void addPendingTransaction(Transaction transaction) {
        pendingTransactionMap.put(transaction.getHash(), transaction);
    }

}
