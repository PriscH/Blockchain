package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import com.prisch.util.Result
import com.prisch.util.Success
import org.springframework.stereotype.Repository

@Repository
class TransactionRepository {

    private val pendingTransactionMap = mutableMapOf<String, JsonNode>()

    fun addTransaction(transaction: JsonNode): Result {
        // TODO: Add validations

        synchronized(pendingTransactionMap) {
            val hash = transaction.get(TransactionField.HASH.nodeName).asText()
            pendingTransactionMap.put(hash, transaction)

            return Success
        }
    }

    fun removeTransaction(transactionHash: String) {
        synchronized(pendingTransactionMap) {
            pendingTransactionMap.remove(transactionHash)
        }
    }
}