package com.github.timebetov.microblog.exception;

import com.github.timebetov.microblog.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> notFoundHandler(ResourceNotFoundException e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false))
                .errorCode(HttpStatus.NOT_FOUND)
                .errorMessage(e.getMessage())
                .errorTime(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<?> alreadyExistsHandler(AlreadyExistsException e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false))
                .errorCode(HttpStatus.BAD_REQUEST)
                .errorMessage(e.getMessage())
                .errorTime(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exceptionHandler(Exception e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false))
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorMessage(e.getMessage())
                .errorTime(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
