package com.prisch.reference.transactions;

import com.prisch.reference.services.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Component
public class TransactionInputBuilder {

    @Autowired private KeyService keyService;

    public List<Transaction.Input> buildInputs(int totalAmount, Collection<Transaction> unclaimedTransactions) {
        List<Transaction.Input> inputs = new LinkedList<>();

        Iterator<Transaction> transactionIterator = unclaimedTransactions.iterator();
        int amountRemaining = totalAmount;

        while (amountRemaining > 0) {
            Transaction transaction = transactionIterator.next();
            int transactionAmount = transaction.getOutputs().stream().filter(out -> out.getAddress().equals(keyService.getAddress()))
                    .mapToInt(Transaction.Output::getAmount)
                    .sum();

            Transaction.Input input = new Transaction.Input();
            input.setTransactionHash(transaction.getHash());
            input.setAddress(keyService.getAddress());
            input.setAmount(transactionAmount);
            inputs.add(input);

            amountRemaining -= transactionAmount;
        }

        return inputs;
    }

}
