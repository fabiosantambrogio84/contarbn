package com.contarbn.model.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Builder
@Data
public class ErrorResponse {

    private String code;
    private String message;
    private String level;
    @JsonIgnore
    private HttpStatus httpStatus;
}
