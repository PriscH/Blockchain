package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import com.prisch.util.Result
import com.prisch.util.Success
import org.springframework.stereotype.Repository

@Repository
class BlockRepository {

    private val blockchain = mutableListOf<JsonNode>()

    fun addBlock(block: JsonNode): Result {
        synchronized(blockchain) {
            // TODO: Add validations

            blockchain.add(block)
            return Success
        }
    }
}