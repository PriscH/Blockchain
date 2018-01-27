package com.prisch.transactions;

import com.prisch.messages.LocalMessages;
import com.prisch.services.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {

    @Autowired private LocalMessages localMessages;
    @Autowired private KeyService keyService;

    private final Map<String, Transaction> pendingTransactionMap = new ConcurrentHashMap<>();
    private final Map<String, Transaction> unclaimedTransactionMap = new ConcurrentHashMap<>();

    public void addPendingTransaction(Transaction transaction) {
        pendingTransactionMap.put(transaction.getHash(), transaction);
    }

    public void acceptTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            pendingTransactionMap.remove(transaction.getHash());
            transaction.getInputs().forEach(inp -> unclaimedTransactionMap.remove(inp.getTransactionHash()));

            if (transaction.getOutputs().stream().anyMatch(out -> out.getAddress().equals(keyService.getAddress()))) {
                unclaimedTransactionMap.put(transaction.getHash(), transaction);
            }
        }
    }

    public void syncPendingTransactions(List<Transaction> transactions) {
        pendingTransactionMap.clear();
        transactions.forEach(this::addPendingTransaction);

        localMessages.addMessage(String.format("Synchronized the pending transactions: received %d transaction(s).", transactions.size()));
    }

    public void syncUnclaimedTransactions(List<Transaction> transactions) {
        unclaimedTransactionMap.clear();

        Set<String> depositTransactionHashes
                = transactions.stream()
                              .filter(tx -> tx.getOutputs().stream().anyMatch(out -> out.getAddress().equals(keyService.getAddress())))
                              .map(Transaction::getHash)
                              .collect(Collectors.toSet());

        Set<String> claimedTransactionHashes
                = transactions.stream()
                              .flatMap(tx -> tx.getInputs().stream())
                              .map(Transaction.Input::getTransactionHash)
                              .collect(Collectors.toSet());

        List<Transaction> unclaimedTransactions
                = transactions.stream()
                              .filter(tx -> depositTransactionHashes.contains(tx.getHash()))
                              .filter(tx -> !claimedTransactionHashes.contains(tx.getHash()))
                              .collect(Collectors.toList());

        unclaimedTransactions.forEach(tx -> unclaimedTransactionMap.put(tx.getHash(), tx));
    }

    public List<Transaction> getMostProfitableTransactions(int count) {
        return pendingTransactionMap.values().stream()
                                    .sorted(Comparator.comparingInt(Transaction::getFeeAmount).reversed())
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

    public Collection<Transaction> getUnclaimedTransactions() {
        return unclaimedTransactionMap.values();
    }
}
