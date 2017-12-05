package com.prisch.ignore.commands;

import com.prisch.ignore.StompSessionHolder;
import com.prisch.ignore.blockchain.BlockchainIndex;
import com.prisch.ignore.blocks.BlockRepository;
import com.prisch.reference.services.KeyService;
import com.prisch.assignments.assignment6.TransactionRepository;
import com.prisch.reference.blocks.Block;
import com.prisch.reference.transactions.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ShellComponent
@ShellCommandGroup("Wallet")
public class WalletCommands {

    @Autowired private KeyService keyService;
    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private BlockchainIndex blockchainIndex;
    @Autowired private BlockRepository blockRepository;
    @Autowired private TransactionRepository transactionRepository;

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

        return String.format("Client Address (%s): %d epicoins\n\n", ownAddress, blockchainIndex.getAddressBalance(ownAddress))
                + otherAddresses.stream().map(adr -> String.format("%s: %d epicoins", adr, blockchainIndex.getAddressBalance(adr)))
                                .collect(Collectors.joining("\n"));
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

    @ShellMethod("Print the block, transaction or address matching the provided hash.")
    public String print(String hash) {
        Optional<String> item = blockchainIndex.get(hash);
        if (!item.isPresent()) {
            item = transactionRepository.getTransaction(hash)
                                        .map(tx -> "!!! TRANSACTION IS PENDING !!!\n\n" + tx + "\n\n!!! TRANSACTION IS PENDING !!!");
        }

        return item.orElse("The provided hash does not match any block, transaction or known address.");
    }

    @ShellMethod("Prints all the blocks")
    public String printBlocks() {
        List<Block> blocks = blockRepository.getBlocks();

        return blocks.stream().map(blk -> String.format("Height: %d, Hash: %s, Transactions: [%s]",
                                                        blk.getHeight(),
                                                        blk.getHash(),
                                                        blk.getTransactions().stream().map(Transaction::getHash).collect(Collectors.joining(", "))))
                              .collect(Collectors.joining("\n"));
    }

    @ShellMethod("Prints all the pending transactions")
    public String printPendingTransactions() {
        Collection<Transaction> transactions = transactionRepository.getTransactions();

        return transactions.stream().map(tx -> String.format("Hash: %s, Output Addresses: [%s]",
                                                             tx.getHash(),
                                                             tx.getOutputs().stream().map(Transaction.Output::getAddress).collect(Collectors.joining(", "))))
                                    .collect(Collectors.joining("\n"));
    }

    @ShellMethodAvailability({"print", "printBlocks", "printPendingTransactions"})
    public Availability printHashAvailabilityCheck() {
        if (!stompSessionHolder.isConnected()) {
            return Availability.unavailable("your client is not connected to the epicoin network (use 'connect' to connect).");
        }

        if (blockchainIndex.isEmpty()) {
            return Availability.unavailable("you have not received the blockchain yet (wait a while or try 'resync').");
        }

        return Availability.available();
    }
}
