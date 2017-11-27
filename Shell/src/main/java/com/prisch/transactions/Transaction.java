package com.prisch.transactions;

import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.Properties;

@Value.Immutable
public interface Transaction {

    int version();

    TransactionReference input();
    Output output();

    String hash();
    String signature();
    String publicKey();

    // lockHeight
    // stopJaco
    Properties properties();

    @Value.Immutable
    interface TransactionReference {
        int blockHeight();
        String transactionHash();

        int outputIndex();
    }

    @Value.Immutable
    interface Output {
        int index();

        String address();
        BigDecimal amount();
    }
}
