package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import com.prisch.util.Result
import com.prisch.util.Success
import org.springframework.stereotype.Repository

@Repository
class TransactionRepository {

    private val pendingTransactionMap = mutableMapOf<String, JsonNode>()

    @Synchronized
    fun addTransaction(transaction: JsonNode): Result {
        // TODO: Add validations

        val hash = transaction.get(TransactionField.HASH.nodeName).asText()
        pendingTransactionMap.put(hash, transaction)

        return Success
    }

    @Synchronized
    fun removeTransaction(transactionHash: String) {
        pendingTransactionMap.remove(transactionHash)
    }

    @Synchronized
    fun getTransactions(): List<JsonNode> {
        return pendingTransactionMap.values.toList()
    }
}