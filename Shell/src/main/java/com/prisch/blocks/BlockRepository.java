package com.prisch.blocks;

import com.prisch.blockchain.BlockchainIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

@Repository
public class BlockRepository {

    @Autowired private BlockchainIndex blockchainIndex;

    private final List<Block> blockchain = new LinkedList<>();

    public synchronized void addBlock(Block block) {
        blockchain.add(block);
        blockchainIndex.add(block);
    }

    public synchronized void syncBlocks(List<Block> blocks) {
        blockchain.clear();
        blockchain.addAll(blocks);
        blockchainIndex.sync(blocks);
    }

    public synchronized Block getTopBlock() {
        return blockchain.get(0);
    }

    public boolean isEmpty() {
        return blockchainIndex.isEmpty();
    }
}
