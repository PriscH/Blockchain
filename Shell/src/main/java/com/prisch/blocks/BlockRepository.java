package com.prisch.blocks;

import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

@Repository
public class BlockRepository {

    private final List<Block> blockchain = new LinkedList<>();

    public void addBlock(Block block) {
        synchronized (blockchain) {
            blockchain.add(block);
        }
    }

    public Block getTopBlock() {
        synchronized (blockchain) {
            Block block = new Block();

            // TODO: Pretty much everything

            block.setHeight(1);
            block.setHash("12345678");

            return block;
        }
    }
}
