package com.prisch.transactions;

import com.prisch.global.Constants;

import java.util.Collections;
import java.util.HashMap;

public class Coinbase extends Transaction {

    public Coinbase(int version, Output output) {
        Input generationInput = new Input();
        generationInput.setBlockHeight(0);
        generationInput.setTransactionHash(Constants.COINBASE_ADDRESS);
        generationInput.setAddress(Constants.COINBASE_ADDRESS);
        generationInput.setAmount(Constants.COINBASE_REWARD);

        super.setInputs(Collections.singletonList(generationInput));
        super.setOutputs(Collections.singletonList(output));

        super.setVersion(version);
        super.setFeeAmount(0);
        super.setHash(Constants.COINBASE_ADDRESS);
        super.setSignature("");
        super.setPublicKey("");
        super.setProperties(new HashMap<>());
    }
}
