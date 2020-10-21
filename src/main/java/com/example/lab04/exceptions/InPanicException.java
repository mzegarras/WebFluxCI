package com.example.lab04.exceptions;

public class InPanicException extends RuntimeException {

    private static final long serialVersionUID = 6922159999398000261L;

    public InPanicException() {
        super("In Panic attack");
    }
}
