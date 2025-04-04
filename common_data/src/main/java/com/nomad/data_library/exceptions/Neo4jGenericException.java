package com.nomad.data_library.exceptions;

public class Neo4jGenericException extends RuntimeException {

    public Neo4jGenericException(String reason, Throwable cause) {
        super(reason + " Exception: " + cause.getMessage(), cause);
      }
}
