package com.prisch.ignore.transactions;

import com.prisch.reference.services.HashService;
import com.prisch.reference.transactions.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionHasher {

    @Autowired private HashService hashService;

    public String hash(List<Transaction.Input> inputs, List<Transaction.Output> outputs, int feeAmount) {
        StringBuilder serializationBuilder = new StringBuilder();

        inputs.forEach(in -> serializationBuilder.append(in.getTransactionHash()));

        outputs.forEach(out -> serializationBuilder.append(out.getAddress())
                .append(out.getAmount()));

        serializationBuilder.append(feeAmount);

        String serializedTransaction = serializationBuilder.toString();
        return hashService.hash(serializedTransaction);
    }

}
