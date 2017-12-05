package com.prisch.ignore.blocks;

import com.prisch.assignments.assignment6.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.stream.Collectors;

@Component
public class BlockSyncHandler extends StompSessionHandlerAdapter {

    @Autowired private BlockRepository blockRepository;
    @Autowired private TransactionRepository transactionRepository;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return BlockSync.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        BlockSync blockSync = (BlockSync)payload;
        blockRepository.syncBlocks(blockSync);
        transactionRepository.syncUnclaimedTransactions(blockSync.stream().flatMap(blk -> blk.getTransactions().stream())
                                                                 .collect(Collectors.toList()));
    }
}
