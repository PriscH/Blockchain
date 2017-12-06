package com.prisch.support

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.prisch.communication.PlainMessage
import com.prisch.communication.ResponseType
import com.prisch.util.State
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import java.security.Principal

@Controller
class SettingController(
        private val state: State,
        private val messageOperations: SimpMessageSendingOperations) {

    @MessageMapping("/settings")
    @SendToUser("/queue/messages")
    fun updateSettings(@RequestBody message: JsonNode, principal: Principal): PlainMessage {
        val notifyClients =
                state.hashCheck != message["hashCheck"].asText()
                || state.transactionLimit != message["transactionLimit"].asInt()

        state.version = message["version"].asInt()
        state.hashCheck = message["hashCheck"].asText()
        state.transactionLimit = message["transactionLimit"].asInt()
        state.allowMessages = message["allowMessages"].asBoolean()
        state.allowBlocks = message["allowBlocks"].asBoolean()
        state.allowTransactions = message["allowTransactions"].asBoolean()

        if (notifyClients) {
            val settings = jacksonObjectMapper().createObjectNode()
            settings.put("hashCheck", state.hashCheck)
            settings.put("transactionLimit", state.transactionLimit)
            messageOperations.convertAndSend("/topic/settings", settings)
        }

        return PlainMessage(ResponseType.INFO, "Settings changed as specified.")
    }

}