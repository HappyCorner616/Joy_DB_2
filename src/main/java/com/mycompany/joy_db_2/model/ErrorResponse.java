package com.mycompany.joy_db_2.model;

public class ErrorResponse {

    private String error;

    public ErrorResponse() {
    }

    public ErrorResponse(String error) {
        this.error = error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
    
    @Override
    public String toString() {
        return error;
    }
}
