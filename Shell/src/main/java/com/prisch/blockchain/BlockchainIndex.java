package com.prisch.blockchain;

import com.prisch.blocks.Block;
import com.prisch.global.Constants;
import com.prisch.transactions.Transaction;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class BlockchainIndex {

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
            return Optional.of(String.format("Address containing %d epicoins.", addressBalanceMap.get(hash)));

        return Optional.empty();
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
