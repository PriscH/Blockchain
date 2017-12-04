package com.prisch.commands;

import com.prisch.services.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Wallet")
public class PrintAddressCommand {

    @Autowired private KeyService keyService;

    @ShellMethod("Print your epicoin address based on your public key.")
    public String printAddress() throws Exception {
        return keyService.getAddress();
    }

    private Availability printAddressAvailability() {
        return (keyService.checkKeysExist())
                ? Availability.available()
                : Availability.unavailable("you do not have a key pair yet (use 'generate-keys' to generate them).");
    }
}
