package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.global.Settings;
import com.prisch.socket.PlainMessageHandler;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.TimeUnit;

@ShellComponent
@ShellCommandGroup("Network")
public class ConnectCommand {

    private static final long TIMEOUT = 5000; //ms

    @Autowired
    private WebSocketStompClient stompClient;

    @Autowired
    private StompSessionHolder stompSessionHolder;

    @Autowired
    private PlainMessageHandler plainMessageHandler;

    @ShellMethod("Connect to the epicoin network")
    public String connect() throws Exception {
        if (stompSessionHolder.isConnected()) {
            return "The client is already connected to the epicoin network.";
        }

        ListenableFuture<StompSession> stompSessionFuture = stompClient.connect(Settings.HOST_URL, new ConnectionHandler());
        try {
            StompSession stompSession = stompSessionFuture.get(TIMEOUT, TimeUnit.MILLISECONDS);
            stompSessionHolder.setStompSession(stompSession);

            return "Successfully connected to the epicoin network.";
        } catch (Exception ex) {
            return new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                    .append("ERROR: ")
                    .style(AttributedStyle.DEFAULT)
                    .append("Unable to connect to the epicoin network.")
                    .style(AttributedStyle.DEFAULT)
                    .toAnsi();
        }
    }

    private class ConnectionHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            session.subscribe("/user/queue/messages", plainMessageHandler);

            session.send("/app/registerClient", Settings.NAME);
        }
    }
}
