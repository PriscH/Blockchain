package com.prisch.ignore.blockchain;

import com.prisch.reference.Constants;
import com.prisch.reference.services.KeyService;
import com.prisch.reference.blocks.Block;
import com.prisch.reference.transactions.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BlockchainIndex {

    @Autowired private KeyService keyService;

    private Map<String, Block> blockMap = new HashMap<>();
    private Map<String, Transaction> transactionMap = new HashMap<>();
    private AddressBalanceMap addressBalanceMap = new AddressBalanceMap();

    public synchronized void add(Block block) {
        blockMap.put(block.getHash(), block);
        block.getTransactions().forEach(tx -> transactionMap.put(tx.getHash(), tx));

        recalculateBalances(block.getTransactions());
    }

    public synchronized void sync(List<Block> blocks) {
        blockMap = new HashMap<>();
        transactionMap = new HashMap<>();
        addressBalanceMap = new AddressBalanceMap();

        blocks.forEach(this::add);
    }

    private void recalculateBalances(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            transaction.getInputs().stream()
                       .filter(inp -> !inp.getAddress().equalsIgnoreCase(Constants.COINBASE_ADDRESS))
                       .forEach(inp -> addressBalanceMap.subtract(inp.getAddress(), inp.getAmount()));

            transaction.getOutputs().forEach(out -> addressBalanceMap.add(out.getAddress(), out.getAmount()));
        }
    }

    public Optional<String> get(String hash) {
        if (blockMap.containsKey(hash))
            return Optional.of(blockMap.get(hash).toJson());

        if (transactionMap.containsKey(hash))
            return Optional.of(transactionMap.get(hash).toJson());

        if (addressBalanceMap.containsKey(hash))
            return Optional.of(buildAddressPrintout(hash));

        return Optional.empty();
    }

    public String buildAddressPrintout(String address) {
        String balance = String.format("Address containing %d epicoins.", addressBalanceMap.get(address));

        String deposits = "Deposit transactions: "
                        + blockMap.values().stream()
                                  .sorted(Comparator.comparingInt(Block::getHeight))
                                  .flatMap(blk -> blk.getTransactions().stream())
                                  .filter(tx -> tx.getOutputs().stream().anyMatch(out -> out.getAddress().equals(address)))
                                  .map(tx -> String.format("{%s: %d epicoin(s)}", tx.getHash(),
                                                    tx.getOutputs().stream().filter(out -> out.getAddress().equals(address))
                                                                   .mapToInt(Transaction.Output::getAmount)
                                                                   .sum()))
                                  .collect(Collectors.joining(", "));

        String withdrawals = "Withdrawal transactions: "
                           + blockMap.values().stream()
                                     .sorted(Comparator.comparingInt(Block::getHeight))
                                     .flatMap(blk -> blk.getTransactions().stream())
                                     .filter(tx -> tx.getInputs().stream().anyMatch(inp -> inp.getAddress().equals(address)))
                                     .map(tx -> String.format("{%s: %d epicoin(s)}", tx.getHash(),
                                                    tx.getInputs().stream().filter(inp -> inp.getAddress().equals(address))
                                                                  .mapToInt(Transaction.Input::getAmount)
                                                                  .sum()))
                                     .collect(Collectors.joining(", "));

        return balance + "\n" + deposits + "\n" + withdrawals;
    }

    public int getAddressBalance(String address) {
        return addressBalanceMap.get(address);
    }

    public Set<String> getKnownAddresses() {
        return addressBalanceMap.keySet();
    }

    public boolean isEmpty() {
        return blockMap.isEmpty();
    }
}
