package com.prisch.assignments.assignment2;

import com.prisch.assignments.Settings;
import com.prisch.ignore.StompSessionHolder;
import com.prisch.ignore.blocks.BlockRepository;
import com.prisch.reference.blocks.Block;
import com.prisch.reference.services.HashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Blockchain")
public class BlockCommands {

    private static final Logger LOG = LoggerFactory.getLogger(BlockCommands.class);

    @Autowired private BlockRepository blockRepository;
    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private HashService hashService;

    @ShellMethod("Post a block to the blockchain")
    public void postBlock() {
        Block lastBlock = blockRepository.getLastBlock();

        // TODO: [2A]
        // Create a block that follows on the last block
        // Ensure that the height is correct and that the previous hash matches the last block
        // You can ignore the nonce for now

        Block proposedBlock = new Block();
        
        LOG.info(proposedBlock.toJson());
        stompSessionHolder.getStompSession().send("/app/postBlock", proposedBlock);
    }

    private Availability postBlockAvailability() {
        if (Settings.VERSION != 2) {
            return Availability.unavailable("posting a block without mining is only supported in version 2");
        }

        if (!stompSessionHolder.isConnected()) {
            return Availability.unavailable("your client is not connected to the epicoin network (use 'connect' to connect).");
        }

        return Availability.available();
    }

    private void hashBlock(Block proposedBlock) {
        // TODO: [2B]
        // Hash the block in whatever way you want as long as you end up with a strategy that results in unique hashes
    }
}
