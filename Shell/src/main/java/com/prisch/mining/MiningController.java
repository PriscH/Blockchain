package com.prisch.mining;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class MiningController {

    @Autowired private BeanFactory beanFactory;
    @Autowired private TaskExecutor taskExecutor;

    private boolean miningActive;
    private volatile Miner miner;

    public boolean isMiningActive() {
        return miningActive;
    }

    public void startMining() {
        miningActive = true;

        if (miner != null) {
            return;
        }

        miner = beanFactory.getBean(Miner.class);
        taskExecutor.execute(miner);
    }

    public void stopMining() {
        miningActive = false;
        if (miner != null) {
            miner.interrupt();
            miner = null;
        }
    }

    public String checkMining() {
        if (miner == null) {
            return "The miner has completed or was interrupted.";
        }

        return String.format("The last nonce checked is %d (previous block: %s).", miner.getNonce(), miner.getPreviousBlockHash());
    }

    public void completeMining() {
        if (miner != null) {
            miner.interrupt();
            miner = null;
        }
    }

    // Received a new block, we need to start over
    public void resetMining() {
        if (miningActive) {
            completeMining();
            startMining();
        }
    }
}
