package com.prisch.ignore.shell;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ShellLineReader {

    @Autowired
    private ApplicationContext applicationContext;

    private LineReader internalReader;

    public String readLine() throws UserInterruptException, EndOfFileException {
        String line = getInternalReader().readLine();
        System.out.println();
        return line;
    }

    public String readLine(String prompt) throws UserInterruptException, EndOfFileException {
        String line = getInternalReader().readLine(prompt);
        System.out.println();
        return line;
    }

    private org.jline.reader.LineReader getInternalReader() {
        if (internalReader == null) {
            internalReader = LineReaderBuilder.builder()
                                              .terminal(applicationContext.getBean(Terminal.class))
                                              .build();
        }

        return internalReader;
    }
}
