package com.prisch.blockchain

import com.prisch.client.ClientRepository
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody

@Controller
class BlockchainController(private val clientRepository: ClientRepository) {

    @MessageMapping("/postTransaction")
    @SendTo("/topic/transactions")
    fun postTransaction(@RequestBody message: Message): String {
        if (message.clientName == "xxx") throw IllegalArgumentException()
        return """{"content": "Everybody party now"}"""
    }

    @MessageMapping("/postBlock")
    @SendTo("/topic/blocks")
    fun postBlock(@RequestBody message: Message): String {
        if (message.clientName == "xxx") throw IllegalArgumentException()
        return """{"content": "Everybody party now"}"""
    }

    @MessageExceptionHandler
    @SendTo("/topic/public")
    fun handleException(exception: Exception): String {
        return """{"content": "Bad time for party"}"""
    }
}