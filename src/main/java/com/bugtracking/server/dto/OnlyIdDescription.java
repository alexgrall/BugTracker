package com.bugtracking.server.dto;

public class OnlyIdDescription {

    private long id;

    public OnlyIdDescription(long id) {
        this.id = id;
    }

    public OnlyIdDescription() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
