package com.prisch.commands;

import com.prisch.StompSessionHolder;
import com.prisch.blocks.BlockRepository;
import com.prisch.mining.MiningController;
import com.prisch.services.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Blockchain")
public class MineCommands {

    @Autowired private MiningController miningController;
    @Autowired private StompSessionHolder stompSessionHolder;
    @Autowired private BlockRepository blockRepository;
    @Autowired private KeyService keyService;

    @ShellMethod("Start mining epicoins")
    public String startMining() throws Exception {
        miningController.startMining();
        return "Mining started.";
    }

    private Availability startMiningAvailability() {
        if (miningController.isMiningActive()) {
            return Availability.unavailable("mining has already been started on your client (use 'stop-mining' to stop).");
        }

        if (!stompSessionHolder.isConnected()) {
            return Availability.unavailable("your client is not connected to the epicoin network (use 'connect' to connect).");
        }

        if (blockRepository.isEmpty()) {
            return Availability.unavailable("you have not received the blockchain yet (wait a while or try 'resync').");
        }

        if (!keyService.checkKeysExist()) {
            return Availability.unavailable("you have to generate a key pair first (use 'generate-keys' to generate them).");
        }

        return Availability.available();
    }

    @ShellMethod("Stop mining epicoins")
    public String stopMining() throws Exception {
        miningController.stopMining();
        return "Mining stopped";
    }

    private Availability stopMiningAvailability() {
        return (miningController.isMiningActive())
                ? Availability.available()
                : Availability.unavailable("mining has not been started on your client (use 'start-mining' to mine).");
    }

    @ShellMethod("Prints the last nonce value checked")
    public String checkMining() throws Exception {
        return miningController.checkMining();
    }

    private Availability checkMiningAvailability() {
        return (miningController.isMiningActive())
                ? Availability.available()
                : Availability.unavailable("mining has not been started on your client (use 'start-mining' to mine).");
    }
}
