package com.prisch.commands;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@ShellCommandGroup("Blockchain")
public class MineCommand {

    @ShellMethod("Start mining epicoins")
    public String startMining() throws Exception {
        return null;
    }

    @ShellMethod("Stop mining epicoins")
    public String stopMining() throws Exception {
        return null;
    }
}
