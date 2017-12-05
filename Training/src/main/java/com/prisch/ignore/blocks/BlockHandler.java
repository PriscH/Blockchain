package com.prisch.ignore.blocks;

import com.prisch.ignore.messages.MessageHolder;
import com.prisch.reference.blocks.Block;
import com.prisch.ignore.mining.MiningController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class BlockHandler extends StompSessionHandlerAdapter {

    @Autowired private BlockRepository blockRepository;
    @Autowired private MiningController miningController;
    @Autowired private MessageHolder messageHolder;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Block.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Block block = (Block)payload;

        blockRepository.addBlock(block);
        messageHolder.addMessage(String.format("Received a new block (%s)", block.getHash()));

        miningController.resetMining();
    }
}
