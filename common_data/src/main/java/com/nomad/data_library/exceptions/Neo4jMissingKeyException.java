package com.nomad.data_library.exceptions;

public class Neo4jMissingKeyException extends RuntimeException {

    public Neo4jMissingKeyException(String mapperFunction) {
        super("Unable to map object. This is probably due to the fact that you forgot to return a record entirely or gave it the wrong key name. Mapping function is: " + mapperFunction);
    }
}
