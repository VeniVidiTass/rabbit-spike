package com.rabbit.bridge.mongorabbitbridge.exception;

import java.io.Serial;

/**
 * Thrown when an AppointmentDto does not contain a valid patient email.
 */
public class MissingEmailException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception with a specific detail message.
     *
     * @param message detail message explaining which appointment was invalid
     */
    public MissingEmailException(String message) {
        super(message);
    }
}
