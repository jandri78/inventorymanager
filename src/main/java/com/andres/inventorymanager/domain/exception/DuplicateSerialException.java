package com.andres.inventorymanager.domain.exception;

public class DuplicateSerialException extends RuntimeException {

    public DuplicateSerialException(String serial) {
        super("An asset with serial '" + serial + "' already exists");
    }
}
