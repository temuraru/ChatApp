package com.temuraru.Exceptions;

public class ForbiddenNameException extends Exception {
    public ForbiddenNameException() {
        super();
    }
    public ForbiddenNameException(String message) {
        super(message);
    }
}
