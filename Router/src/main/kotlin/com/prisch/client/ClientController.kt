package com.prisch.client

import com.prisch.communication.ActionResponse
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
    fun registerClient(@Payload clientRegistration: ClientRegistration, principal: Principal): ActionResponse {
        val validationResult = clientRegistration.validate()
        if (validationResult is Failure)
            return ActionResponse(ResponseType.ERROR, validationResult.message)

        val registrationResult = clientRepository.registerClient(clientRegistration, principal)
        if (registrationResult is Failure)
            return ActionResponse(ResponseType.ERROR, registrationResult.message)

        return ActionResponse(ResponseType.SUCCESS, "Successfully registered your client with name '${clientRegistration.name}'")
    }
}