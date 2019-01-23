package com.bugtracking.server.domain.exceptions;

public class ObjectNotFoundException extends Exception {

    public final String objectType;
    public final Object objectId;

    public ObjectNotFoundException(String objectType, Object objectId) {
        super(generateMessage(objectType, objectId));
        this.objectType = objectType;
        this.objectId = objectId;
    }

    public ObjectNotFoundException(Class objectType, Object objectId) {
        this(objectType.getSimpleName(), objectId);
    }

    private static String generateMessage(String objectType, Object objectId) {
        return objectType + " with ID " + objectId + " does not exist.";
    }
}
