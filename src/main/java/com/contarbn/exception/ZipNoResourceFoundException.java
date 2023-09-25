package com.contarbn.exception;

import com.contarbn.util.enumeration.Resource;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class ZipNoResourceFoundException extends RuntimeException {

    public ZipNoResourceFoundException(Resource resource) {
        super(String.format("Nessuna %s ancora da inviare presente nelle date selezionate", resource.getLabel()));
    }

}
