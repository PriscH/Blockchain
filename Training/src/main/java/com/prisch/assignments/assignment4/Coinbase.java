package com.prisch.assignments.assignment4;

import com.prisch.assignments.Settings;
import com.prisch.reference.Constants;
import com.prisch.reference.services.HashService;
import com.prisch.reference.services.KeyService;
import com.prisch.reference.transactions.Transaction;

import java.util.Collections;
import java.util.HashMap;

public class Coinbase extends Transaction {

    public static Coinbase with(int proposedHeight, int transactionFees, KeyService keyService, HashService hashService) {
        // TODO: [4A]
        // Create the Coinbase transaction.
        // Important here is to specify an output that has as its amount the sum of the {@link Constants.COINBASE_REWARD} and transactionFees
        // Most people would send this output to their own address, but whatever, its your reward money, do with it what you want

        return null;
    }

    private Coinbase(int proposedHeight, Output output, HashService hashService) {
        Input generationInput = new Input();
        generationInput.setTransactionHash(Constants.COINBASE_ADDRESS);
        generationInput.setAddress(Constants.COINBASE_ADDRESS);
        generationInput.setAmount(Constants.COINBASE_REWARD);

        String transactionHash = hashService.hash(String.valueOf(proposedHeight));
        super.setHash(transactionHash);

        super.setInputs(Collections.singletonList(generationInput));
        super.setOutputs(Collections.singletonList(output));

        super.setVersion(Settings.VERSION);
        super.setFeeAmount(0);
        super.setSignature("");
        super.setPublicKey("");
        super.setProperties(new HashMap<>());
    }
}
