package com.prisch.blockchain

enum class TransactionField(val version: Int, val nodeName: String) {

    VERSION                (0, "version"),
    INPUTS                 (0, "inputs"),
    OUTPUTS                (0, "outputs"),
    FEE_AMOUNT             (0, "feeAmount"),
    HASH                   (0, "hash"),
    SIGNATURE              (0, "signature"),
    PUBLIC_KEY             (0, "publicKey"),
    PROPERTIES             (0, "properties"),
    LOCK_HEIGHT            (0, "LOCK_HEIGHT"),
    STOP_JACO              (0, "STOP_JACO"),

    INPUT_BLOCK_HEIGHT     (0, "blockHeight"),
    INPUT_TRANSACTION_HASH (0, "transactionHash"),
    INPUT_ADDRESS          (0, "address"),
    INPUT_AMOUNT           (0, "amount"),

    OUTPUT_ADDRESS         (0, "address"),
    OUTPUT_AMOUNT          (0, "amount");
}