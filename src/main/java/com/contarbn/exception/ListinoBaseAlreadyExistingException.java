package com.contarbn.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class ListinoBaseAlreadyExistingException extends RuntimeException {

    public ListinoBaseAlreadyExistingException() {
        super("Un listino BASE e' gia' presente nel sistema");
    }
}
