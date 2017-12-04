package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.blockchain.BlockchainIndex;
import com.prisch.blocks.Block;
import com.prisch.blocks.BlockRepository;
import com.prisch.services.KeyService;
import com.prisch.transactions.Transaction;
import com.prisch.transactions.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

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
}
