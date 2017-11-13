package com.prisch.websocket

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener {

    @EventListener
    fun handleConnect(event: SessionConnectEvent) {
        System.out.println(event)
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {

    }
}