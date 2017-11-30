package com.prisch.client

import com.prisch.util.Failure
import com.prisch.util.Result
import com.prisch.util.Success
import org.springframework.stereotype.Repository
import java.security.Principal

@Repository
class ClientRepository {

    private val clientMap = mutableMapOf<Principal, String>()

    fun registerClient(clientName: String, principal: Principal): Result {
        synchronized(clientMap) {
            if (clientMap.any { it.key != principal && it.value == clientName })
                return Failure("That name is already in use by someone else.")

            clientMap.put(principal, clientName)
            return Success
        }
    }

    fun deregisterClient(principal: Principal) {
        synchronized(clientMap) {
            clientMap.remove(principal)
        }
    }
}