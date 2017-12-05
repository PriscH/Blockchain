package com.prisch.blockchain;

import org.springframework.stereotype.Component;

@Component
public class BlockchainProperties {

    private volatile String hashCheck = "++++A";
    private volatile int transactionLimit = 3;

    public String getHashCheck() {
        return hashCheck;
    }

    public void setHashCheck(String hashCheck) {
        this.hashCheck = hashCheck;
    }

    public int getTransactionLimit() {
        return transactionLimit;
    }

    public void setTransactionLimit(int transactionLimit) {
        this.transactionLimit = transactionLimit;
    }
}
