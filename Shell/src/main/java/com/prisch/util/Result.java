package com.prisch.util;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class Result<T> {

    private final T value;
    private final String failureMessage;

    private Result(T value, String failureMessage) {
        this.value = value;
        this.failureMessage = failureMessage;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(String failureMessage) {
        return new Result<>(null, failureMessage);
    }

    public static <T> Result<T> failure(String failureMessageTemplate, Object... parameters) {
        return failure(String.format(failureMessageTemplate, parameters));
    }

    public boolean isSuccess() {
        return value != null;
    }

    public T get() {
        return value;
    }

    public String getFailureMessage() {
        return new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                                            .append("INVALID INPUT: ")
                                            .style(AttributedStyle.DEFAULT)
                                            .append(failureMessage)
                                            .toAnsi();
    }
}


