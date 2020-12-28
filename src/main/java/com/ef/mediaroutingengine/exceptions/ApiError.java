package com.ef.mediaroutingengine.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Arrays;
import java.util.List;

public class ApiError {

    private HttpStatus status;
    private String message;
    private List<String> errors;


    public ApiError(HttpStatus status){
        this.status = status;

    }
    public  ApiError(HttpStatus status, String message){
        this.status=status;
        this.message = message;
    }

    public  ApiError(HttpStatus status, String message, List<String> errors){
        this.status=status;
        this.message = message;
        this.errors = errors;
    }

    public  ApiError(HttpStatus status, String message, String error){
        this(status,message);
        errors = Arrays.asList(error);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
}
