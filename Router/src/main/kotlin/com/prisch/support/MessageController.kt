package com.prisch.support

import com.fasterxml.jackson.databind.JsonNode
import com.prisch.client.ClientRepository
import com.prisch.communication.PlainMessage
import com.prisch.communication.ResponseType
import com.prisch.util.State
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import java.security.Principal

@Controller
class MessageController(
        private val messageOperations: SimpMessageSendingOperations,
        private val clientRepository: ClientRepository,
        private val state: State) {

    val LOG = LoggerFactory.getLogger(MessageController::class.java)

    @MessageMapping("/postMessage")
    fun postMessage(@RequestBody message: JsonNode, principal: Principal) {
        if (!state.allowMessages) {
            messageOperations.convertAndSendToUser(principal.name, "/queue/messages", PlainMessage(ResponseType.WARNING, "Messaging is currently disabled."))
        } else {
            LOG.info("MESSAGE = ${clientRepository.getClientName(principal)} : $message")
            messageOperations.convertAndSend("/topic/messages", message)
        }
    }

}