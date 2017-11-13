package com.prisch.client

import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ClientRepository {

    private val clientSnapshotMap = mutableMapOf<String, ClientSnapshot>()

    fun refreshClient(clientDetails: ClientDetails) {
        val clientSnapshot = ClientSnapshot(clientDetails, LocalDateTime.now())
        clientSnapshotMap.put(clientDetails.name, clientSnapshot)
    }

    fun activeClients() = clientSnapshotMap.values.filter { it.lastRefreshTime.isAfter(cutoffTimestamp()) }
                                                  .map { it.clientDetails }

    private fun cutoffTimestamp() = LocalDateTime.now().minusSeconds(20)
}