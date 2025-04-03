package com.nomad.scraping_library.exceptions;

public class ScrapingDataSchemaException extends RuntimeException {

    public ScrapingDataSchemaException(String reason, Throwable e) {
        super(reason, e);
    }

    public ScrapingDataSchemaException(String reason) {
        super(reason);
    }
}
