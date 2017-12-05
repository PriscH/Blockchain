package com.prisch.assignments.assignment8;

import com.prisch.reference.Constants;
import com.prisch.reference.blocks.Block;
import org.springframework.stereotype.Component;

@Component
public class JacoBlockValidator {

    public boolean validate(Block block) {
        if (block.getProperties().containsKey(Constants.STOP_JACO_KEY)) {
            // TODO: [8D]
            // Decide whether you are part of the pro-Jaco fork or anti-Jaco fork
        }

        // TODO: [8E]
        // Strictly speaking if you are on the anti-Jaco fork you have to check that none of the Block transactions contain an input
        // originating from the banished address '//NzHW4=', but this still is optional

        return true;
    }

}
