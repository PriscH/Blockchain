package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.messages.Configuration;
import com.prisch.shell.ShellLineReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Configuration")
public class ConfigurationCommands {

    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private ShellLineReader shellLineReader;

    @ShellMethod("Update configuration on the server.")
    public void updateConfiguration() {
        Configuration configuration = new Configuration();

        configuration.setVersion(Integer.valueOf(shellLineReader.readLine("Version: ")));
        configuration.setHashCheck(shellLineReader.readLine("Hash Check: "));
        configuration.setTransactionLimit(Integer.valueOf(shellLineReader.readLine("Transaction Limit: ")));
        configuration.setAllowMessages(Boolean.valueOf(shellLineReader.readLine("Allow Messages: ")));
        configuration.setAllowBlocks(Boolean.valueOf(shellLineReader.readLine("Allow Blocks: ")));
        configuration.setAllowTransactions(Boolean.valueOf(shellLineReader.readLine("Allow Transactions: ")));

        stompSessionHolder.getStompSession().send("/app/settings", configuration);
    }

    @ShellMethod("Load the Blockchain from the backup.")
    public void loadBlockchain() {
        stompSessionHolder.getStompSession().send("/app/loadBlockchain", "");
    }

    private Availability updateConfigurationAvailability() {
        if (!stompSessionHolder.isConnected()) {
            return Availability.unavailable("your client is not connected to the epicoin network (use 'connect' to connect).");
        }

        return Availability.available();
    }
}
