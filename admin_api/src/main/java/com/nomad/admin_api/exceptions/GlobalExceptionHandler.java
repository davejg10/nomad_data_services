package com.nomad.admin_api.exceptions;

import java.util.Map;
import java.util.Optional;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class GlobalExceptionHandler {

    public static HttpResponseMessage handleDataIntegrityViolation(
            DuplicateEntityException ex,
            HttpRequestMessage<Optional<String>> request) {

        String userMessage;
        if (GeoEntity.CITY.equals(ex.getEntityType())) {
            userMessage = "City: " + ex.getEntityName() + ", already exists for this country. Please use the update endpoint instead.";
        } else { 
            userMessage = "Country: " + ex.getEntityName() + ", already exists. Please use the update endpoint instead.";
        }
        log.info("Caught DuplicateEntityException with entity type: [{}]. Returning HTTP 409.", ex.getEntityType());

        // Return a structured error response if possible
        Map<String, Object> body = Map.of(
                "status", HttpStatus.CONFLICT.value(),
                "message", userMessage,
                "entityId", ex.getEntityId()
        );

        return request.createResponseBuilder(HttpStatus.CONFLICT).body(body).build();
    }
}
