package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.mining.MiningController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Network")
public class NetworkCommands {

    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private MiningController miningController;

    @ShellMethod("Resyncs the blockchain and any pending transactions")
    public String resync() {
        stompSessionHolder.getStompSession().send("/app/sync", "resync");
        return "Resync request sent.";
    }

    public Availability resyncAvailability() {
        if (!stompSessionHolder.isConnected()) {
            return Availability.unavailable("your client is not connected to the epicoin network (use 'connect' to connect).");
        }

        if (miningController.isMiningActive()) {
            return Availability.unavailable("mining is currently active on your client (use 'stop-mining' to stop).");
        }

        return Availability.available();
    }
}
