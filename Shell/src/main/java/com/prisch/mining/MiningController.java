package com.prisch.mining;

import org.springframework.stereotype.Component;

@Component
public class MiningController {

    public boolean miningActive;

    public boolean isMiningActive() {
        return miningActive;
    }

    public void setMiningActive(boolean miningActive) {
        this.miningActive = miningActive;
    }

    public void startMining() {

    }

}
