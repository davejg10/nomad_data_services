package com.nomad.admin_api.exceptions;

public class DuplicateEntityException extends RuntimeException {

    private final GeoEntity entityType;
    private final String entityId;
    private final String entityName;

    public DuplicateEntityException(String message, GeoEntity entityType, String entityId, String entityName) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
    }

    public GeoEntity getEntityType() {
        return entityType;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityId() {
        return entityId;
    }
}
