package com.prisch.websocket

import org.springframework.http.server.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal
import java.util.*

class WebSocketHandshakeHandler : DefaultHandshakeHandler() {

    override fun determineUser(request: ServerHttpRequest?, wsHandler: WebSocketHandler?, attributes: MutableMap<String, Any>?): Principal {
        return UsernamePasswordAuthenticationToken(UUID.randomUUID().toString().toUpperCase(), "")
    }
}