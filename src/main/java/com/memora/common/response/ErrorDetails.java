package com.memora.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error payload structure returned when an exception occurs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;
    private Instant timestamp;
    private List<ValidationError> validationErrors;

    public ErrorDetails() {
        this.success = false;
        this.timestamp = Instant.now();
    }

    public ErrorDetails(int status, String error, String message, String path) {
        this.success = false;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public ErrorDetails(int status, String error, String message, String path, List<ValidationError> validationErrors) {
        this.success = false;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
        this.validationErrors = validationErrors;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
