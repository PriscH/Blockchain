package com.prisch.blocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class BlockSyncHandler extends StompSessionHandlerAdapter {

    @Autowired private BlockRepository blockRepository;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return BlockSync.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        BlockSync blockSync = (BlockSync)payload;
        blockRepository.syncBlocks(blockSync);
    }
}
