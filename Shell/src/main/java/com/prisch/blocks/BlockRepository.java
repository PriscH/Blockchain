package com.prisch.blocks;

import com.prisch.blockchain.BlockchainIndex;
import com.prisch.messages.MessageHolder;
import com.prisch.transactions.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BlockRepository {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BlockchainIndex blockchainIndex;
    @Autowired private MessageHolder messageHolder;

    private final List<Block> blockchain = new ArrayList<>();

    public synchronized void addBlock(Block block) {
        blockchain.add(block);
        blockchainIndex.add(block);

        transactionRepository.acceptTransactions(block.getTransactions());
    }

    public synchronized void syncBlocks(List<Block> blocks) {
        blockchain.clear();
        blockchain.addAll(blocks);
        blockchainIndex.sync(blocks);

        messageHolder.addMessage(String.format("Synchronized the blockchain: received %d block(s).", blocks.size()));
    }

    public synchronized Block getLastBlock() {
        return blockchain.get(blockchain.size() - 1);
    }

    public synchronized List<Block> getBlocks() {
        return blockchain;
    }

    public boolean isEmpty() {
        return blockchainIndex.isEmpty();
    }
}
