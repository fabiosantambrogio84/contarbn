package com.contarbn.handler;

import com.contarbn.exception.OperationException;
import com.contarbn.model.beans.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ServiceExceptionHandler {

    @ExceptionHandler(OperationException.class)
    public ResponseEntity<ErrorResponse> handleOperationException(final OperationException operationException){
        log.error(operationException.getMessage(), operationException);
        return createErrorResponseEntity(operationException.getMessage(), operationException.getHttpStatus());
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(final String message, final HttpStatus httpStatus){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .httpStatus(httpStatus)
                .message(message)
                .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
