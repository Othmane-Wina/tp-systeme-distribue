package com.demo.restapi.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    // 1. RESOURCE_NOT_FOUND (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex, Locale locale) {
        // Try to get the translated message. {0} will be replaced by the ID.
        String errorMessage;
        try {
            errorMessage = messageSource.getMessage("product.notfound", new Object[]{ex.getMessage()}, locale);
        } catch (NoSuchMessageException e) {
            // Fallback if the key is missing in properties
            errorMessage = ex.getMessage();
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", errorMessage);
        body.put("path", "/api/v1/products");

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 2. BODY_NOT_VALID (400)
    // Triggered when @Valid fails on a @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyInvalid(MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse(
                "BODY_NOT_VALID",
                "The JSON body is missing required fields or has invalid formats.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. PARAMS_NOT_VALID (400)
    // Triggered for invalid URL parameters or Type mismatches
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleParamsInvalid(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "PARAMS_NOT_VALID",
                "The URL parameters provided are invalid or missing.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 4. PATH_NOT_FOUND (404)
    // Triggered when a user hits a URL that doesn't exist
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handlePathNotFound(NoHandlerFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                "PATH_NOT_FOUND",
                "The requested URL path does not exist.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 5. SERVER_ERROR (500)
    // The "Catch-All" for any unexpected crashes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "SERVER_ERROR",
                "Something went wrong on our end. Please try again later.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
