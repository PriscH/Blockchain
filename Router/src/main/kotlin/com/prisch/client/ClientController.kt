package com.prisch.client

import com.prisch.util.Failure
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ClientController(private val clientRepository: ClientRepository) {

    @MessageMapping("/addClient")
    @SendToUser("/queue/status")
    fun addClient(@Payload clientDetails: ClientDetails, principal: Principal): String {
        val validationResult = clientDetails.validate()
        if (validationResult is Failure)
            return validationResult.message

        clientRepository.refreshClient(clientDetails)

        return """{"content": "Just for you"}"""
    }
}