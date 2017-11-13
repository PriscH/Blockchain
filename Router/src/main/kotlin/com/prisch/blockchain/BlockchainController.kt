package com.prisch.blockchain

import com.prisch.client.ClientRepository
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody

@Controller
class BlockchainController(private val clientRepository: ClientRepository) {

    @MessageMapping("/postTransaction")
    @SendTo("/topic/public")
    fun postTransaction(@RequestBody message: Message): String {
        return """{"content": "Everybody party now"}"""
    }

}