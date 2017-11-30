package com.prisch.client

import com.prisch.communication.PlainMessage
import com.prisch.communication.ResponseType
import com.prisch.util.Failure
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ClientController(private val clientRepository: ClientRepository) {

    @MessageMapping("/registerClient")
    @SendToUser("/queue/messages")
    fun registerClient(@Payload clientName: String, principal: Principal): PlainMessage {
        if (clientName.length !in 3..20)
            return PlainMessage(ResponseType.ERROR, "Your client name should be between 3 and 20 characters long.")

        val registrationResult = clientRepository.registerClient(clientName, principal)
        if (registrationResult is Failure)
            return PlainMessage(ResponseType.ERROR, registrationResult.message)

        return PlainMessage(ResponseType.INFO, "Successfully registered your client with name '$clientName'")
    }
}