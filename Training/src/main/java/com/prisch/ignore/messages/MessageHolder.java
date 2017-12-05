package com.prisch.ignore.messages;

import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MessageHolder {

    private final List<String> unreadMessages = new LinkedList<>();
    private List<String> readMessages = new LinkedList<>();

    public synchronized void addMessage(String message) {
            unreadMessages.add(message);
    }

    public synchronized void printUnreadMessages() {
        printMessages(unreadMessages);
        readMessages.addAll(unreadMessages);
        unreadMessages.clear();
    }

    public void printAllMessages() {
        printMessages(readMessages);
    }

    public int getUnreadMessageCount() {
        return unreadMessages.size();
    }

    private void printMessages(List<String> messages) {
        System.out.println("\n" + messages.stream().collect(Collectors.joining("\n")));
    }
}
