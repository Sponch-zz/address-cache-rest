package com.address.cache.model;

/**
 * Created by cesponc on 6/15/16.
 */
public class CacheResponse {

    private String message;

    public CacheResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
