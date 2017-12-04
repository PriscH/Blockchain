package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.blocks.BlockHandler;
import com.prisch.global.Settings;
import com.prisch.messages.PlainMessageHandler;
import com.prisch.transactions.TransactionHandler;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.TimeUnit;

@ShellComponent
@ShellCommandGroup("Network")
public class ConnectCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectCommand.class);

    private static final long TIMEOUT = 5000; //ms

    @Autowired private WebSocketStompClient stompClient;
    @Autowired private StompSessionHolder stompSessionHolder;

    @Autowired private PlainMessageHandler plainMessageHandler;
    @Autowired private TransactionHandler transactionHandler;
    @Autowired private BlockHandler blockHandler;

    @ShellMethod("Connect to the epicoin network")
    public String connect() throws Exception {
        ListenableFuture<StompSession> stompSessionFuture = stompClient.connect(Settings.HOST_URL, new ConnectionHandler());
        try {
            StompSession stompSession = stompSessionFuture.get(TIMEOUT, TimeUnit.MILLISECONDS);
            stompSessionHolder.setStompSession(stompSession);

            return "Successfully connected to the epicoin network.";
        } catch (Exception ex) {
            LOG.error("Unable to connect to the epicoin network", ex);

            return new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                    .append("ERROR: ")
                    .style(AttributedStyle.DEFAULT)
                    .append("Unable to connect to the epicoin network.")
                    .style(AttributedStyle.DEFAULT)
                    .toAnsi();
        }
    }

    private Availability connectAvailability() {
        return (!stompSessionHolder.isConnected())
                ? Availability.available()
                : Availability.unavailable("your client is already connected to the epicoin network.");
    }

    private class ConnectionHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            session.subscribe("/user/queue/messages", plainMessageHandler);
            session.subscribe("/topic/transactions", transactionHandler);
            session.subscribe("/topic/blocks", blockHandler);

            session.send("/app/registerClient", Settings.NAME);
        }
    }
}
