package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import com.prisch.client.ClientRepository
import com.prisch.communication.PlainMessage
import com.prisch.communication.ResponseType
import com.prisch.util.Failure
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import java.security.Principal

@Controller
class BlockchainController(val clientRepository: ClientRepository,
                           val transactionRepository: TransactionRepository,
                           val blockRepository: BlockRepository,
                           val messageOperations: SimpMessageSendingOperations) {

    val LOG = LoggerFactory.getLogger(BlockchainController::class.java)

    @MessageMapping("/postTransaction")
    fun postTransaction(@RequestBody transaction: JsonNode, principal: Principal) {
        LOG.info("TRANSACTION = ${clientRepository.getClientName(principal)} : $transaction")
        val result = transactionRepository.addTransaction(transaction)

        if (result is Failure) {
            messageOperations.convertAndSendToUser(principal.name, "/queue/messages", PlainMessage(ResponseType.ERROR, result.message))
        } else {
            val response = "Your transaction (${transaction.get(TransactionField.HASH.nodeName).asText()}) was accepted."
            messageOperations.convertAndSendToUser(principal.name, "/queue/messages", PlainMessage(ResponseType.INFO, response))
            messageOperations.convertAndSend("/topic/transactions", transaction)
        }
    }

    @MessageMapping("/postBlock")
    fun postBlock(@RequestBody block: JsonNode, principal: Principal) {
        LOG.info("BLOCK = ${clientRepository.getClientName(principal)} : $block")
        val result = blockRepository.addBlock(block)

        if (result is Failure) {
            messageOperations.convertAndSendToUser(principal.name, "/queue/messages", PlainMessage(ResponseType.ERROR, result.message))
        } else {
            val response = "Your block (${block.get(BlockField.HASH.nodeName).asText()}) was accepted."
            messageOperations.convertAndSendToUser(principal.name, "/queue/messages", PlainMessage(ResponseType.INFO, response))
            messageOperations.convertAndSend("/topic/blocks", block)
        }
    }

    @MessageMapping("/sync")
    fun synchronize(principal: Principal) {
        LOG.info("SYNC = ${clientRepository.getClientName(principal)}")

        messageOperations.convertAndSendToUser(principal.name, "/queue/blocks", blockRepository.getBlocks())
        messageOperations.convertAndSendToUser(principal.name, "/queue/transactions", transactionRepository.getTransactions())
    }

    @MessageExceptionHandler
    @SendToUser("/queue/messages")
    fun handleException(exception: Exception): PlainMessage {
        return PlainMessage(ResponseType.ERROR, "You managed to break the server.\n Please call Jaco, he is going to be sad :(\n");
    }
}