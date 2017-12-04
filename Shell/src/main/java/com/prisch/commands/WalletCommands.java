package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.blockchain.BlockchainIndex;
import com.prisch.services.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;
import java.util.stream.Collectors;

@ShellComponent
@ShellCommandGroup("Wallet")
public class WalletCommands {

    @Autowired private KeyService keyService;
    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private BlockchainIndex blockchainIndex;

    @ShellMethod("Print your epicoin address based on your public key.")
    public String printAddress() throws Exception {
        return keyService.getAddress();
    }

    private Availability printAddressAvailability() {
        return (keyService.checkKeysExist())
                ? Availability.available()
                : Availability.unavailable("you do not have a key pair yet (use 'generate-keys' to generate them).");
    }

    @ShellMethod("Print the balances of all known addresses.")
    public String printBalances() throws Exception {
        String ownAddress = keyService.getAddress();

        List<String> otherAddresses = blockchainIndex.getKnownAddresses().stream()
                                                     .filter(adr -> !ownAddress.equals(adr))
                                                     .sorted()
                                                     .collect(Collectors.toList());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Client Address (%s): %d epicoins", ownAddress, blockchainIndex.getAddressBalance(ownAddress)))
                     .append("\n\n");

        otherAddresses.forEach(adr -> stringBuilder.append(String.format("%s: %d epicoins\n", adr, blockchainIndex.getAddressBalance(adr))));

        return stringBuilder.toString();
    }

    private Availability printBalancesAvailability() {
        if (!keyService.checkKeysExist()) {
            return Availability.unavailable("you have to generate a key pair first (use 'generate-keys' to generate them).");
        }

        if (!stompSessionHolder.isConnected()) {
            return Availability.unavailable("your client is not connected to the epicoin network (use 'connect' to connect).");
        }

        if (blockchainIndex.isEmpty()) {
            return Availability.unavailable("you have not received the blockchain yet (wait a while or try 'resync').");
        }

        return Availability.available();
    }
}
