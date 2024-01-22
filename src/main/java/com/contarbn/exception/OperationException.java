package com.contarbn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
public class OperationException extends RuntimeException{

    private HttpStatus httpStatus;

    public OperationException(String message){
        super(message);
        this.httpStatus = INTERNAL_SERVER_ERROR;
    }

    public OperationException(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus = httpStatus;
    }
}
