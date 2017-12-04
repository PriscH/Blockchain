package com.prisch.transactions;

import com.prisch.messages.MessageHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {

    @Autowired private MessageHolder messageHolder;

    private final Map<String, Transaction> pendingTransactionMap = new ConcurrentHashMap<>();
    private final Map<String, Transaction> unclaimedClientTransactionMap = new ConcurrentHashMap<>();

    public void addPendingTransaction(Transaction transaction) {
        pendingTransactionMap.put(transaction.getHash(), transaction);
    }

    public void removePendingTransactions(List<Transaction> transactions) {
        transactions.forEach(tx -> pendingTransactionMap.remove(tx.getHash()));
    }

    public void syncPendingTransactions(List<Transaction> transactions) {
        pendingTransactionMap.clear();
        transactions.forEach(this::addPendingTransaction);

        messageHolder.addMessage(String.format("Synchronized the pending transactions: received %d transaction(s).", transactions.size()));
    }

    public List<Transaction> getMostProfitableTransactions(int count) {
        return pendingTransactionMap.values().stream()
                                    .sorted(Comparator.comparingInt(Transaction::getFeeAmount))
                                    .limit(count)
                                    .collect(Collectors.toList());
    }

    public Optional<String> getTransaction(String hash) {
        if (pendingTransactionMap.containsKey(hash))
            return Optional.of(pendingTransactionMap.get(hash).toJson());

        return Optional.empty();
    }

    public Collection<Transaction> getTransactions() {
        return pendingTransactionMap.values();
    }
}
