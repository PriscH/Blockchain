package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.prisch.util.Failure
import com.prisch.util.Result
import com.prisch.util.State
import com.prisch.util.Success
import org.springframework.stereotype.Repository

@Repository
class BlockRepository(
        private val state: State,
        private val transactionRepository: TransactionRepository,
        private val blockchainIndex: BlockchainIndex) {

    private final val COINBASE_ADDRESS = "00000000"
    private final val COINBASE_REWARD = 100
    private final val TRANSACTION_LIMIT = 3

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

        return Success
    }

    @Synchronized
    fun getBlocks(): List<JsonNode> {
        return blockchain.toList()
    }

    private fun validate(block: JsonNode): Result {
        if (block.get(BlockField.VERSION.nodeName).asInt() != state.version)
            return Failure("The epicoin network is at version ${state.version}.")

        val previousBlockValidation = validateAgainstPreviousBlock(block)
        if (previousBlockValidation is Failure)
            return previousBlockValidation

        val transactionValidation = validateTransactions(block)
        if (transactionValidation is Failure)
            return transactionValidation

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

        val coinbaseCount = transactions.count(coinbaseCheck)

        if (coinbaseCount == 0)
            return Failure("Your block must include a coinbase transaction.")

        if (coinbaseCount > 1)
            return Failure("Your block is only allowed to contain a single coinbase transaction")

        val coinbase = transactions.find(coinbaseCheck)!!

        val feeAmount = transactions.map{ it.get(TransactionField.FEE_AMOUNT.nodeName).asInt() }.sum()
        val coinbaseValidation = validateCoinbase(coinbase, feeAmount)
        if (coinbaseValidation is Failure)
            return coinbaseValidation

        if (transactions.size() > TRANSACTION_LIMIT)
            return Failure("A block may only contain $TRANSACTION_LIMIT, including the coinbase transaction.")

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
}