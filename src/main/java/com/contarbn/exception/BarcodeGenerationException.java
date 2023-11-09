package com.contarbn.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class BarcodeGenerationException extends RuntimeException {

    public BarcodeGenerationException(String message) {
        super(message);
    }
}
