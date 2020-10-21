package com.example.lab04.exceptions;

public class UnauthorizedException extends RuntimeException {
    //private static final long serialVersionUID = 3669724432146593613L;

    public UnauthorizedException() {
        super("Unauthorized call");
    }
}
