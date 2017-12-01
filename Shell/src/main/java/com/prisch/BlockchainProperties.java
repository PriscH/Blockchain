package com.prisch;

import org.springframework.stereotype.Component;

@Component
public class BlockchainProperties {

    private volatile String hashCheck;

    public String getHashCheck() {
        return hashCheck;
    }

    public void setHashCheck(String hashCheck) {
        this.hashCheck = hashCheck;
    }
}
