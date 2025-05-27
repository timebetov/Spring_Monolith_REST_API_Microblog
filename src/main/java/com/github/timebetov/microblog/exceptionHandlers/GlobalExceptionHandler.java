package com.github.timebetov.microblog.exceptionHandlers;

import com.github.timebetov.microblog.dtos.ErrorResponseDTO;
import com.github.timebetov.microblog.exceptions.AlreadyExistsException;
import com.github.timebetov.microblog.exceptions.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.BAD_REQUEST)
                .errorDetails(errors)
                .errorTime(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.NOT_FOUND)
                .errorTime(LocalDateTime.now())
                .errorMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.FORBIDDEN)
                .errorTime(LocalDateTime.now())
                .errorMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.BAD_REQUEST)
                .errorTime(LocalDateTime.now())
                .errorMessage(ex.getMessage())
                .build();

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

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.BAD_REQUEST)
                .errorDetails(errors)
                .errorTime(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.BAD_REQUEST)
                .errorTime(LocalDateTime.now())
                .errorMessage(ex.getName())
                .build();

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> notFoundHandler(ResourceNotFoundException e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.NOT_FOUND)
                .errorMessage(e.getMessage())
                .errorTime(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> alreadyExistsHandler(AlreadyExistsException e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.BAD_REQUEST)
                .errorMessage(e.getMessage())
                .errorTime(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> exceptionHandler(Exception e, WebRequest request) {

        ErrorResponseDTO result = ErrorResponseDTO.builder()
                .apiPath(request.getDescription(false).replace("uri=", ""))
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorMessage(e.getMessage())
                .errorTime(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
