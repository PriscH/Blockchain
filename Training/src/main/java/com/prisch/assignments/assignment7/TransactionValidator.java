package com.prisch.assignments.assignment7;

import com.prisch.assignments.assignment1.SignatureVerifier;
import com.prisch.ignore.transactions.TransactionHasher;
import com.prisch.reference.services.HashService;
import com.prisch.reference.transactions.Transaction;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionValidator {

    @Autowired
    private HashService hashService;
    @Autowired private TransactionHasher transactionHasher;
    @Autowired private SignatureVerifier signatureVerifier;

    public boolean validate(Transaction transaction) {
        // TODO: [7A]
        // Up until now we've just trusted that the person posting the transaction actually owns the address
        // Let's check it ourselves, good thing we created that SignatureVerifier earlier on
        // The steps are basically:
        // 1) Check whether the public key matches the input address (remember the address is derived from the public key)
        // 2) Verify that the transaction hash is correct
        // 3) Use the public key to verify the signature against the transaction hash
        // Hint: Have a look at what TransactionCommands is doing, seeing as it is responsible for signing the transaction

        boolean result = true;

        // TODO: [7+]
        // Perform the other transaction validations as well. At a high level:
        // 1) Ensure that the input amounts match the output amounts
        // 2) Ensure that the inputs have not been used yet

        if (!result) {
            System.out.println(new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                                                            .append("ERROR: ")
                                                            .style(AttributedStyle.DEFAULT)
                                                            .append("An incoming transaction failed validation:\n")
                                                            .toAnsi());

            System.out.println(new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
                                                            .append(transaction.toJson())
                                                            .style(AttributedStyle.DEFAULT)
                                                            .toAnsi());
        }

        return result;
    }

}
