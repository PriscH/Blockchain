package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode
import com.prisch.client.ClientRepository
import com.prisch.communication.PlainMessage
import com.prisch.communication.ResponseType
import com.prisch.util.Failure
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageSendingOperations
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
        LOG.info("TRANSACTION = ${principal.name} : $transaction")
        val result = transactionRepository.addTransaction(transaction)

        if (result is Failure) {
            messageOperations.convertAndSendToUser(principal.name, "/queue/messages", PlainMessage(ResponseType.ERROR, result.message))
        } else {
            messageOperations.convertAndSend("/topic/transactions", transaction)
        }
    }

    @MessageMapping("/postBlock")
    fun postBlock(@RequestBody block: JsonNode, principal: Principal) {
        LOG.info("BLOCK = ${principal.name} : $block")
        val result = blockRepository.addBlock(block)

        if (result is Failure) {
            messageOperations.convertAndSendToUser(principal.name, "/queue/messages", PlainMessage(ResponseType.ERROR, result.message))
        } else {
            messageOperations.convertAndSend("/topic/blocks", block)
        }
    }

    @MessageExceptionHandler
    @SendTo("/topic/public")
    fun handleException(exception: Exception): String {
        return """{"content": "Bad time for party"}"""
    }
}