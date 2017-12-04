package com.prisch.transactions;

import com.prisch.global.Constants;
import com.prisch.services.HashService;

import java.util.Collections;
import java.util.HashMap;

public class Coinbase extends Transaction {

    public Coinbase(int version, int proposedHeight, Output output, HashService hashService) {
        Input generationInput = new Input();
        generationInput.setBlockHeight(proposedHeight);
        generationInput.setTransactionHash(Constants.COINBASE_ADDRESS);
        generationInput.setAddress(Constants.COINBASE_ADDRESS);
        generationInput.setAmount(Constants.COINBASE_REWARD);

        String transactionHash = hashService.hash(String.valueOf(proposedHeight));
        super.setHash(transactionHash);

        super.setInputs(Collections.singletonList(generationInput));
        super.setOutputs(Collections.singletonList(output));

        super.setVersion(version);
        super.setFeeAmount(0);
        super.setSignature("");
        super.setPublicKey("");
        super.setProperties(new HashMap<>());
    }
}
