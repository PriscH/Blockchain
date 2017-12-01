package com.prisch.messages;

import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MessageHolder {

    private final List<String> unreadMessages = new LinkedList<>();
    private List<String> readMessages = new LinkedList<>();

    private final Object lock = new Object();

    public void addMessage(String message) {
        synchronized (lock) {
            unreadMessages.add(message);
        }
    }

    public void printUnreadMessages() {
        synchronized (lock) {
            printMessages(unreadMessages);
            readMessages.addAll(unreadMessages);
            unreadMessages.clear();
        }
    }

    public void printAllMessages() {
        printMessages(readMessages);
    }

    public int getUnreadMessageCount() {
        synchronized (lock) {
            return unreadMessages.size();
        }
    }

    private void printMessages(List<String> messages) {
        System.out.println("\n" + messages.stream().collect(Collectors.joining("\n")));
    }
}
