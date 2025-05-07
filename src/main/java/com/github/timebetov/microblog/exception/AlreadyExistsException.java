package com.github.timebetov.microblog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s already exists with the given input data %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
