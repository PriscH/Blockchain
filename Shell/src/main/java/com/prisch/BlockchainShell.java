package com.prisch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@SpringBootApplication
public class BlockchainShell {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BlockchainShell.class, args);
    }

    @Bean
    public WebSocketStompClient webSocketStompClient() {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
        return stompClient;
    }

    /*
    @Bean
    public WebSocketConnectionManager wsConnectionManager() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketHandler handler = new MessageHandler();
        WebSocketConnectionManager manager = new WebSocketConnectionManager(client, handler, "ws://localhost:8080/ws");
        return manager;
    }
    */
}
