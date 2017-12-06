package com.prisch.messages;

public class Configuration {

    private int version;
    private String hashCheck;
    private int transactionLimit;
    private boolean allowMessages;
    private boolean allowBlocks;
    private boolean allowTransactions;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

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

    public boolean isAllowMessages() {
        return allowMessages;
    }

    public void setAllowMessages(boolean allowMessages) {
        this.allowMessages = allowMessages;
    }

    public boolean isAllowBlocks() {
        return allowBlocks;
    }

    public void setAllowBlocks(boolean allowBlocks) {
        this.allowBlocks = allowBlocks;
    }

    public boolean isAllowTransactions() {
        return allowTransactions;
    }

    public void setAllowTransactions(boolean allowTransactions) {
        this.allowTransactions = allowTransactions;
    }
}
