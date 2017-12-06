package com.prisch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class ShutdownHook {

    @Autowired private StompSessionHolder stompSessionHolder;

    @PreDestroy
    public void shutdown() {
        if (stompSessionHolder.isConnected()) {
            stompSessionHolder.getStompSession().disconnect();
        }
    }
}
