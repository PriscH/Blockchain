package com.prisch.blocks;

import com.prisch.mining.MiningController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class BlockHandler extends StompSessionHandlerAdapter {

    @Autowired private BlockRepository blockRepository;
    @Autowired private MiningController miningController;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Block.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Block block = (Block)payload;
        blockRepository.addBlock(block);
        miningController.resetMining();
    }
}
