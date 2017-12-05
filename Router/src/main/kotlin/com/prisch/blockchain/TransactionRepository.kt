package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import com.prisch.util.*
import org.springframework.stereotype.Repository

@Repository
class TransactionRepository(
        private val state: State,
        private val hasher: Hasher,
        private val encryption: Encryption,
        private val blockchainIndex: BlockchainIndex) {

    private val pendingTransactionMap = mutableMapOf<String, JsonNode>()

    @Synchronized
    fun addTransaction(transaction: JsonNode): Result {
        val validationResult = validate(transaction)
        if (validationResult is Failure)
            return validationResult

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

    private fun validate(transaction: JsonNode): Result {
        if (transaction.get(TransactionField.VERSION.nodeName).asInt() != state.version)
            return Failure("The epicoin network is at version ${state.version}.")

        if (state.version >= TransactionField.INPUTS.version) {
            val inputs = transaction.get(TransactionField.INPUTS.nodeName)

            val addressCheck = getInputAddress(inputs)
            if (addressCheck is Left)
                return Failure(addressCheck.failure)

            val inputValidation = inputs.fold<JsonNode, Result>(Success, { result, input -> result.and(validateInput(input)) })
            if (inputValidation is Failure)
                return inputValidation

            val duplicateSpendingValidation = validateDuplicateSpending(inputs)
            if (duplicateSpendingValidation is Failure)
                return duplicateSpendingValidation
        }

        if (state.version >= TransactionField.OUTPUTS.version) {
            val outputs = transaction.get(TransactionField.OUTPUTS.nodeName)
            val inputs = transaction.get(TransactionField.INPUTS.nodeName)
            val feeAmount = transaction.get(TransactionField.FEE_AMOUNT.nodeName).asInt()

            val outputSpendingValidation = validateOutputSpending(outputs, inputs, feeAmount)
            if (outputSpendingValidation is Failure)
                return outputSpendingValidation

            val outputDuplicatesValidation = validateOutputDuplicates(outputs)
            if (outputDuplicatesValidation is Failure)
                return outputDuplicatesValidation
        }

        val hashValidation = validateHash(transaction)
        if (hashValidation is Failure)
            return hashValidation

        val signatureValidation = validateSignature(transaction)
        if (signatureValidation is Failure)
            return signatureValidation

        return Success
    }

    private fun getInputAddress(inputs: JsonNode): Either<String, String> {
        val address = inputs.first().get(TransactionField.INPUT_ADDRESS.nodeName).asText()

        if (!inputs.all { it.get(TransactionField.INPUT_ADDRESS.nodeName).asText() == address })
            return Left("All transaction inputs should come from the same address.")

        return Right(address)
    }

    private fun validateInput(input: JsonNode): Result {
        val address = input.get(TransactionField.INPUT_ADDRESS.nodeName).asText()
        val transactionHash = input.get(TransactionField.INPUT_TRANSACTION_HASH.nodeName).asText()

        if (!blockchainIndex.isTransactionUnclaimed(address, transactionHash))
            return Failure("The output of transaction $transactionHash has already been used in another block.")

        val transaction = blockchainIndex.getTransaction(transactionHash)
        if (transaction == null)
            return Failure("The input transaction ($transactionHash) doesn't exist.")

        val transactionOutputAmount = transaction.get(TransactionField.OUTPUTS.nodeName)
                                                 .filter{ it.get(TransactionField.OUTPUT_ADDRESS.nodeName).asText() == address }
                                                 .map{ it.get(TransactionField.OUTPUT_AMOUNT.nodeName).asInt() }
                                                 .sum()

        val inputAmount = input.get(TransactionField.INPUT_AMOUNT.nodeName).asInt()
        if (transactionOutputAmount != inputAmount)
            return Failure("The input amount ($inputAmount) should exactly match the previous transations output amount ($transactionOutputAmount).")

        return Success
    }

    private fun validateDuplicateSpending(inputs: JsonNode): Result {
        val inputTransactions = inputs.map { it.get(TransactionField.INPUT_TRANSACTION_HASH.nodeName).asText() }
        if (inputTransactions.size > inputTransactions.distinct().size)
            return Failure("Each transaction can only be used as input once.")

        return Success
    }

    private fun validateOutputSpending(outputs: JsonNode, inputs: JsonNode, feeAmount: Int): Result {
        val outputAmount = outputs.map{ it.get(TransactionField.OUTPUT_AMOUNT.nodeName).asInt() }.sum()
        val inputAmount = inputs.map{ it.get(TransactionField.INPUT_AMOUNT.nodeName).asInt() }.sum()

        if (outputAmount + feeAmount != inputAmount)
            return Failure("The output amount and fees ($outputAmount output + $feeAmount fees) should exactly match the input amount ($inputAmount).")

        return Success
    }

    private fun validateOutputDuplicates(outputs: JsonNode): Result {
        val outputAddresses = outputs.map { it.get(TransactionField.OUTPUT_ADDRESS.nodeName).asText() }
        if (outputAddresses.size > outputAddresses.distinct().size)
            return Failure("Addresses may only be used once as output in every transaction.")

        return Success
    }

    private fun validateHash(transaction: JsonNode): Result {
        val serializationBuilder = StringBuilder()

        transaction.get(TransactionField.INPUTS.nodeName).forEach {
            serializationBuilder.append(it.get(TransactionField.INPUT_TRANSACTION_HASH.nodeName).asText())
        }

        transaction.get(TransactionField.OUTPUTS.nodeName).forEach {
            serializationBuilder.append(it.get(TransactionField.OUTPUT_ADDRESS.nodeName).asText())
                                .append(it.get(TransactionField.OUTPUT_AMOUNT.nodeName).asInt())
        }

        val serializedTransaction = serializationBuilder.toString()
        val expectedHash = hasher.hash(serializedTransaction)
        val actualHash = transaction.get(TransactionField.HASH.nodeName).asText()

        if (expectedHash != actualHash)
            return Failure("The transaction hash is incorrect. Expected $expectedHash but received $actualHash.")

        return Success
    }

    private fun validateSignature(transaction: JsonNode): Result {
        val publicKey = transaction.get(TransactionField.PUBLIC_KEY.nodeName).asText()
        val expectedAddress = hasher.hash(publicKey)
        val actualAddress = transaction.get(TransactionField.INPUTS.nodeName)
                                       .first().get(TransactionField.INPUT_ADDRESS.nodeName).asText()

        if (expectedAddress != actualAddress)
            return Failure("The provided public key does not match the input addresses, expected address is $expectedAddress but actual address is $actualAddress.")

        val hash = transaction.get(TransactionField.HASH.nodeName).asText()
        val signature = transaction.get(TransactionField.SIGNATURE.nodeName).asText()
        if (!encryption.verifySignature(hash, signature, publicKey))
            return Failure("The signature does not match the provided public key and hash.")

        return Success
    }
}