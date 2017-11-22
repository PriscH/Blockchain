package com.prisch.websocket

import com.prisch.client.ClientRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener(private val clientRepository: ClientRepository) {

    @EventListener
    fun handleConnect(event: SessionConnectEvent) {
        println("!!!!!!!!!!!!!!!!! Client successfully connected !!!!!!!!!!!!!!")
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        clientRepository.deregisterClient(event.user)
    }
}