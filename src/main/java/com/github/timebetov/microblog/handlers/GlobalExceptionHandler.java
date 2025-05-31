package com.github.timebetov.microblog.handlers;

import com.github.timebetov.microblog.dtos.ErrorResponseDTO;
import com.github.timebetov.microblog.exceptions.AlreadyExistsException;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        List<ObjectError> validErrors = ex.getBindingResult().getAllErrors();

        validErrors.forEach(error -> {
            String fieldName;
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } else {
                fieldName = error.getObjectName();
            }
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponseDTO result = ErrorResponseDTO.withDetailsAndCode(
                request.getDescription(false),
                HttpStatus.BAD_REQUEST, errors);

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.withMsgAndCode(
                request.getDescription(false),
                HttpStatus.NOT_FOUND, ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.withMsgAndCode(
                request.getDescription(false),
                HttpStatus.FORBIDDEN, ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.withMsgAndCode(
                request.getDescription(false),
                HttpStatus.BAD_REQUEST, ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.withMsgAndCode(
                request.getDescription(false),
                HttpStatus.BAD_REQUEST, ex.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String path = cv.getPropertyPath().toString();
            String message = cv.getMessage();
            errors.put(path, message);
        });

        ErrorResponseDTO result = ErrorResponseDTO.withDetailsAndCode(
                request.getDescription(false),
                HttpStatus.BAD_REQUEST, errors);

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.withMsgAndCode(
                request.getDescription(false),
                HttpStatus.BAD_REQUEST, ex.getMessage()
        );

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> notFoundHandler(ResourceNotFoundException e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.withMsgAndCode(
                request.getDescription(false),
                HttpStatus.NOT_FOUND, e.getMessage());

        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> alreadyExistsHandler(AlreadyExistsException e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.withMsgAndCode(
                request.getDescription(false),
                HttpStatus.BAD_REQUEST, e.getMessage());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> exceptionHandler(Exception e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.withMsgAndCode(
                request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
