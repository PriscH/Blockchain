package com.prisch.assignments.assignment8;

import com.prisch.reference.Constants;
import com.prisch.reference.transactions.Transaction;
import org.springframework.stereotype.Component;

@Component
public class JacoTransactionValidator {

    public boolean validate(Transaction transaction) {
        if (transaction.getProperties().containsKey(Constants.STOP_JACO_KEY)) {
            // TODO: [8B]
            // Decide whether you are part of the pro-Jaco fork or anti-Jaco fork
        }

        // TODO: [8C]
        // If you are part of the anti-Jaco fork:
        // Ensure that none of the inputs originate from the banished address '//NzHW4='

        return true;
    }
}
