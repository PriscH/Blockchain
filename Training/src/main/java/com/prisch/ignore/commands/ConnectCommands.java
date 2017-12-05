package com.prisch.ignore.commands;

import com.prisch.assignments.assignment1.UserMessageHandler;
import com.prisch.ignore.StompSessionHolder;
import com.prisch.ignore.blockchain.BlockchainPropertiesHandler;
import com.prisch.ignore.blocks.BlockHandler;
import com.prisch.ignore.blocks.BlockSyncHandler;
import com.prisch.assignments.Settings;
import com.prisch.ignore.messages.PlainMessageHandler;
import com.prisch.ignore.transactions.TransactionHandler;
import com.prisch.ignore.transactions.TransactionSyncHandler;
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
public class ConnectCommands {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectCommands.class);

    private static final long TIMEOUT = 5000; //ms

    @Autowired private WebSocketStompClient stompClient;
    @Autowired private StompSessionHolder stompSessionHolder;

    @Autowired private PlainMessageHandler plainMessageHandler;
    @Autowired private BlockHandler blockHandler;
    @Autowired private TransactionHandler transactionHandler;
    @Autowired private BlockSyncHandler blockSyncHandler;
    @Autowired private TransactionSyncHandler transactionSyncHandler;
    @Autowired private BlockchainPropertiesHandler blockchainPropertiesHandler;
    @Autowired private UserMessageHandler userMessageHandler;

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

            session.subscribe("/topic/blocks", blockHandler);
            session.subscribe("/topic/transactions", transactionHandler);

            session.subscribe("/user/queue/blocks", blockSyncHandler);
            session.subscribe("/user/queue/transactions", transactionSyncHandler);

            session.subscribe("/topic/settings", blockchainPropertiesHandler);
            session.subscribe("/topic/messages", userMessageHandler);

            session.send("/app/registerClient", Settings.NAME);
            stompSessionHolder.getStompSession().send("/app/sync", "sync");
        }
    }
}
