package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Component

@Component
class BlockchainIndex {

    private val transactionMap = mutableMapOf<String, JsonNode>()
    private val unclaimedAddressTransactionMap = mutableMapOf<String, MutableSet<String>>()

    @Synchronized
    fun processBlock(block: JsonNode) {
        block.get(BlockField.TRANSACTIONS.nodeName).forEach { tx ->
            transactionMap.put(tx.get(TransactionField.HASH.nodeName).asText(), tx)
        }

        block.get(BlockField.TRANSACTIONS.nodeName).forEach { tx ->
            tx.get(TransactionField.OUTPUTS.nodeName).map { out ->
                out.get(TransactionField.OUTPUT_ADDRESS.nodeName).asText()
            }.forEach { addr ->
                unclaimedAddressTransactionMap.getOrPut(addr, { mutableSetOf() })
                                              .add(tx.get(TransactionField.HASH.nodeName).asText())
            }
        }
    }

    @Synchronized
    fun getTransaction(hash: String): JsonNode? {
        return transactionMap[hash]
    }

    @Synchronized
    fun isTransactionUnclaimed(address: String, transactionHash: String): Boolean {
        if (!unclaimedAddressTransactionMap.containsKey(address))
            return false

        return unclaimedAddressTransactionMap[address]!!.contains(transactionHash)

    }
}