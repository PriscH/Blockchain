package com.prisch.ignore;

import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

@Component
public class StompSessionHolder {

    private StompSession stompSession;

    public StompSession getStompSession() {
        return stompSession;
    }

    public void setStompSession(StompSession stompSession) {
        this.stompSession = stompSession;
    }

    public boolean isConnected() {
        return getStompSession() != null;
    }
}
