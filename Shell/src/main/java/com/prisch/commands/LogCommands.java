package com.prisch.commands;

import com.prisch.messages.LocalMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@ShellCommandGroup("Utils")
public class LogCommands {

    @Autowired private LocalMessages localMessages;

    @ShellMethod("Print your background messages.")
    public void printMessages(@ShellOption(defaultValue="false") boolean all) {
        if (all) {
            localMessages.printAllMessages();
        } else {
            localMessages.printUnreadMessages();
        }
    }
}
