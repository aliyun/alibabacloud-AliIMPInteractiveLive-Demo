package com.aliyun.roompaas.base.error;

public enum Warnings implements ErrorMessage{

    PROCESSING("processing, please wait for last request"),
    ;

    private final String message;

    Warnings(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Warnings{" +
                "message='" + message + '\'' +
                '}';
    }
}
