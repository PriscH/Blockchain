package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.prisch.util.*
import org.springframework.stereotype.Repository
import java.io.File

@Repository
class BlockRepository(
        private val state: State,
        private val hasher: Hasher,
        private val transactionRepository: TransactionRepository,
        private val blockchainIndex: BlockchainIndex) {

    private final val COINBASE_ADDRESS = "00000000"
    private final val COINBASE_REWARD = 100
    private final val BLOCKCHAIN_STORE = "blockchain.json"

    private final val blockchain = mutableListOf<JsonNode>()

    init {
        val genesisString = jacksonObjectMapper().writeValueAsString(GenesisBlock())
        blockchain.add(jacksonObjectMapper().readTree(genesisString))
    }

    @Synchronized
    fun addBlock(block: JsonNode): Result {
        val validationResult = validate(block)
        if (validationResult is Failure)
            return validationResult

        blockchain.add(block)
        blockchainIndex.processBlock(block)
        block.get(BlockField.TRANSACTIONS.nodeName).forEach {
            transactionRepository.removeTransaction(it.get(TransactionField.HASH.nodeName).asText())
        }

        persistBlockchain()

        return Success
    }

    @Synchronized
    fun getBlocks(): List<JsonNode> {
        return blockchain.toList()
    }

    @Synchronized
    fun loadBlockchain() {
        blockchain.clear()

        val blocks = jacksonObjectMapper().reader()
                                          .readTree(File(BLOCKCHAIN_STORE).bufferedReader())

        blocks.forEach {
            blockchain.add(it)
            blockchainIndex.processBlock(it)
        }
    }

    private fun persistBlockchain() {
        jacksonObjectMapper().writer()
                             .writeValue(File(BLOCKCHAIN_STORE), blockchain)
    }

    private fun validate(block: JsonNode): Result {
        if (block.get(BlockField.VERSION.nodeName).asInt() != state.version)
            return Failure("The epicoin network is at version ${state.version}.")

        val previousBlockValidation = validateAgainstPreviousBlock(block)
        if (previousBlockValidation is Failure)
            return previousBlockValidation

        if (state.version > 2) {
            val hashCollisionValidation = validateHashCollision(block)
            if (hashCollisionValidation is Failure)
                return hashCollisionValidation

            val transactionValidation = validateTransactions(block)
            if (transactionValidation is Failure)
                return transactionValidation

            val hashValidation = validateHash(block)
            if (hashValidation is Failure)
                return hashValidation
        }

        return Success
    }

    private fun validateHashCollision(block: JsonNode): Result {
        val blockHash = block.get(BlockField.HASH.nodeName).asText()
        if (blockchain.any { it.get(BlockField.HASH.nodeName).asText() == blockHash })
            return Failure("The block hash clashes with an existing block.")

        return Success
    }

    private fun validateAgainstPreviousBlock(block: JsonNode): Result {
        val lastBlock = blockchain.last()

        val lastBlockHeight = lastBlock.get(BlockField.HEIGHT.nodeName).asInt()
        val blockHeight = block.get(BlockField.HEIGHT.nodeName).asInt()
        if (blockHeight != lastBlockHeight + 1)
            return Failure("The last block is at height $lastBlockHeight, any new block should be one higher. Your block is at height $blockHeight")

        val lastBlockHash = lastBlock.get(BlockField.HASH.nodeName).asText()
        val previousHash = block.get(BlockField.PREVIOUS_HASH.nodeName).asText()
        if (lastBlockHash != previousHash)
            return Failure("The last block hash is $lastBlockHash but your block has a the previous hash as $previousHash")

        return Success
    }

    private fun validateTransactions(block: JsonNode): Result {
        val transactions = block.get(BlockField.TRANSACTIONS.nodeName)

        val coinbaseCheck = { tx: JsonNode ->
            tx.get(TransactionField.INPUTS.nodeName)
                    .any { inp -> inp.get(TransactionField.INPUT_ADDRESS.nodeName).asText() == COINBASE_ADDRESS }
        }

        if (state.version > 3) {
            val coinbaseCount = transactions.count(coinbaseCheck)

            if (coinbaseCount == 0)
                return Failure("Your block must include a coinbase transaction.")

            if (coinbaseCount > 1)
                return Failure("Your block is only allowed to contain a single coinbase transaction")

            val coinbase = transactions.find(coinbaseCheck)!!

            val feeAmount = transactions.map { it.get(TransactionField.FEE_AMOUNT.nodeName).asInt() }.sum()
            val coinbaseValidation = validateCoinbase(coinbase, feeAmount)
            if (coinbaseValidation is Failure)
                return coinbaseValidation
        }

        if (transactions.size() > state.transactionLimit)
            return Failure("A block may only contain ${state.transactionLimit}, including the coinbase transaction.")

        val transactionHashes = transactions.map{ it.get(TransactionField.HASH.nodeName).asText() }
        if (transactionHashes.size != transactionHashes.distinct().size)
            return Failure("Each transaction may only be included once.")

        val transactionValidation = transactions.filter{ !coinbaseCheck(it) }
                                                .fold<JsonNode, Result>(Success, { result, tx -> result.and(validateTransaction(tx)) })
        if (transactionValidation is Failure)
            return transactionValidation

        val feeAmountValidation = validateFeeAmount(transactions)
        if (feeAmountValidation is Failure)
            return feeAmountValidation

        return Success
    }

    private fun validateCoinbase(coinbase: JsonNode, feeAmount: Int): Result {
        if (coinbase.get(TransactionField.INPUTS.nodeName).size() != 1)
            return Failure("The coinbase should have exactly one input.")

        val input = coinbase.get(TransactionField.INPUTS.nodeName).first()!!
        if (input.get(TransactionField.INPUT_AMOUNT.nodeName).asInt() != COINBASE_REWARD)
            return Failure("The coinbase input amount has to be exactly the coinbase reward ($COINBASE_REWARD).")

        if (coinbase.get(TransactionField.OUTPUTS.nodeName).size() != 1)
            return Failure("The coinbase should have exactly one output.")

        val output = coinbase.get(TransactionField.OUTPUTS.nodeName).first()!!
        if (output.get(TransactionField.OUTPUT_AMOUNT.nodeName).asInt() != (COINBASE_REWARD + feeAmount))
            return Failure("The coinbase output amount has to be exactly the coinbase reward ($COINBASE_REWARD) and fee amount ($feeAmount).")

        return Success
    }

    private fun validateTransaction(transaction: JsonNode): Result {
        val transactionHash = transaction.get(TransactionField.HASH.nodeName).asText()
        val referenceTransaction = transactionRepository.getTransaction(transactionHash)

        if (referenceTransaction == null)
            return Failure("The transaction with hash $transactionHash is not available any more.")

        if (transaction != referenceTransaction)
            return Failure("The transaction with hash $transaction does not match the details replicated earlier.")

        return Success
    }

    private fun validateFeeAmount(transactions: JsonNode): Result {
        val transactionFees = transactions.map { tx ->
            val inputAmount = tx.get(TransactionField.INPUTS.nodeName)
                                .map { inp -> inp.get(TransactionField.INPUT_AMOUNT.nodeName).asInt() }
                                .sum()

            val outputAmount = tx.get(TransactionField.OUTPUTS.nodeName)
                                 .map { inp -> inp.get(TransactionField.OUTPUT_AMOUNT.nodeName).asInt() }
                                 .sum()

            (inputAmount - outputAmount)
        }.sum()

        if (transactionFees != 0)
            return Failure("The fee amount must match the total of the transaction fees.")

        return Success
    }

    private fun validateHash(block: JsonNode): Result {
        val serializationBuilder = StringBuilder()

        block.get(BlockField.TRANSACTIONS.nodeName).forEach {
            serializationBuilder.append(it.get(TransactionField.HASH.nodeName).asText())
        }

        serializationBuilder.append(block.get(BlockField.PREVIOUS_HASH.nodeName).asText())
        serializationBuilder.append(block.get(BlockField.NONCE.nodeName).asText())

        val serializedTransaction = serializationBuilder.toString()
        val blockHash = hasher.hashWithoutTrunc(serializedTransaction)

        if (!satifiesHashCheck(blockHash)) {
            val blockHashStart = blockHash.substring(0, state.hashCheck.length)
            return Failure("The proposed block does not satisfy the proof of work check. The hash starts with $blockHashStart and needs to be less than ${state.hashCheck}.")
        }

        return Success
    }

    private fun satifiesHashCheck(blockHash: String): Boolean {
        val checkLength = state.hashCheck.length
        val blockHashStart = blockHash.substring(0, checkLength)

        return blockHashStart <= state.hashCheck
    }
}