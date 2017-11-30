package com.prisch.socket;

public class PlainMessage {

    private Type type;
    private String content;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public enum Type {
        INFO,
        WARNING,
        ERROR
    }
}
