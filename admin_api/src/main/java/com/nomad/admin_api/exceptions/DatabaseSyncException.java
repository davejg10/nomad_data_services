package com.nomad.admin_api.exceptions;

public class DatabaseSyncException extends RuntimeException {
    
    public DatabaseSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
