package com.prisch.transactions;

import com.prisch.global.Constants;

import java.util.Collections;
import java.util.HashMap;

public class Coinbase extends Transaction {

    public Coinbase(int version, Output output) {
        Input generationInput = new Input();
        generationInput.setBlockHeight(0);
        generationInput.setTransactionHash("0");
        generationInput.setAmount(Constants.COINBASE_REWARD);

        super.setInputs(Collections.singletonList(generationInput));
        super.setOutputs(Collections.singletonList(output));

        super.setVersion(version);
        super.setFeeAmount(0);
        super.setHash("0");
        super.setSignature("0");
        super.setPublicKey("0");
        super.setProperties(new HashMap<>());
    }
}
