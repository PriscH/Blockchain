package com.prisch.client

import com.prisch.util.Failure
import com.prisch.util.Result
import com.prisch.util.Success
import org.springframework.stereotype.Repository
import java.security.Principal

@Repository
class ClientRepository {

    private val clientMap = mutableMapOf<Principal, Client>()

    fun registerClient(clientRegistration: ClientRegistration, principal: Principal): Result {
        synchronized(clientMap) {
            if (clientMap.any { it.key != principal && it.value.name == clientRegistration.name })
                return Failure("That name is already in use by someone else.")

            clientMap.put(principal, Client(clientRegistration))
            return Success
        }
    }

    fun deregisterClient(principal: Principal) {
        synchronized(clientMap) {
            clientMap.remove(principal)
        }
    }
}