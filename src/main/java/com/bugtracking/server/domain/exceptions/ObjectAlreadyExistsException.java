package com.bugtracking.server.domain.exceptions;

import java.util.Map;

public class ObjectAlreadyExistsException extends Exception {

    public final String objectType;
    public final Map<String, Object> duplicatedBy;
    private final Object existingObjectId;

    public ObjectAlreadyExistsException(String objectType, Map<String, Object> duplicatedBy, Object existingObjectId) {
        super(generateMessage(objectType, duplicatedBy));
        this.objectType = objectType;
        this.duplicatedBy = duplicatedBy;
        this.existingObjectId = existingObjectId;
    }

    public ObjectAlreadyExistsException(Class objectType, Map<String, Object> duplicatedBy, Object existingObjectId) {
        this(objectType.getSimpleName(), duplicatedBy, existingObjectId);
    }

    private static String generateMessage(String objectType, Map<String, Object> duplicatedBy) {
        StringBuilder sb = new StringBuilder(objectType);
        if (duplicatedBy != null && !duplicatedBy.isEmpty()) {
            sb.append(" with ");
            boolean isFirst = true;
            for (Map.Entry<String, Object> duplicatedProperty : duplicatedBy.entrySet()) {
                if (!isFirst) {
                    sb.append(" and ");
                }
                sb.append(duplicatedProperty.getKey()).append(" = ").append(duplicatedProperty.getValue());
                isFirst = false;
            }
        }
        sb.append(" already exists.");
        return sb.toString();
    }

    public Object getExistingObjectId() {
        return existingObjectId;
    }

}
