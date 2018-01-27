package com.prisch.ignore.blockchain;

import com.prisch.ignore.messages.LocalMessages;
import com.prisch.reference.blockchain.BlockchainProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class BlockchainPropertiesHandler extends StompSessionHandlerAdapter {

    @Autowired private BlockchainProperties blockchainProperties;
    @Autowired private LocalMessages localMessages;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return BlockchainProperties.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        BlockchainProperties updatedProperties = (BlockchainProperties)payload;

        blockchainProperties.setHashCheck(updatedProperties.getHashCheck());
        blockchainProperties.setTransactionLimit(updatedProperties.getTransactionLimit());

        localMessages.addMessage(String.format("The blockchain properties have been updated: hashCheck = %s, transactionLimit = %d",
                                                updatedProperties.getHashCheck(),
                                                updatedProperties.getTransactionLimit()));
    }
}
