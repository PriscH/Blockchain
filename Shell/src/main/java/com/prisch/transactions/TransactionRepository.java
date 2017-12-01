package com.prisch.transactions;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {

    private final Map<String, Transaction> pendingTransactionMap = new ConcurrentHashMap<>();

    public void addPendingTransaction(Transaction transaction) {
        pendingTransactionMap.put(transaction.getHash(), transaction);
    }

    public List<Transaction> getMostProfitableTransactions(int count) {
        return pendingTransactionMap.values().stream()
                                    .sorted(Comparator.comparingInt(Transaction::getFeeAmount))
                                    .limit(count)
                                    .collect(Collectors.toList());
    }

}
