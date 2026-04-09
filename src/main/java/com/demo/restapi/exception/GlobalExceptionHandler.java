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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<Map<String, Object>> handleBodyInvalid(MethodArgumentNotValidException ex, Locale locale) {
        // 1. Extract the specific field errors to help with debugging
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        // 2. Try to get the translated general message
        String errorMessage;
        try {
            errorMessage = messageSource.getMessage("validation.error", null, locale);
        } catch (NoSuchMessageException e) {
            errorMessage = "Invalid request content.";
        }

        // 3. Build the body map exactly like handleNotFound
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("errorCode", "BODY_NOT_VALID");
        body.put("message", errorMessage);
        body.put("details", details); // This shows WHICH fields failed
        body.put("path", "/api/v1/products");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 3. PARAMS_NOT_VALID (400)
    // Triggered for invalid URL parameters or Type mismatches
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> handleParamsInvalid(Exception ex, Locale locale) {
        String errorMessage = messageSource.getMessage("params.invalid", null, locale);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("errorCode", "PARAMS_NOT_VALID");
        body.put("message", errorMessage);
        body.put("path", "/api/v1/products");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 4. PATH_NOT_FOUND (404)
    // Triggered when a user hits a URL that doesn't exist
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePathNotFound(NoHandlerFoundException ex, Locale locale) {
        String errorMessage = messageSource.getMessage("path.notfound", null, locale);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("errorCode", "PATH_NOT_FOUND");
        body.put("message", errorMessage);
        body.put("path", ex.getRequestURL());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 5. SERVER_ERROR (500)
    // The "Catch-All" for any unexpected crashes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralError(Exception ex, Locale locale) {
        String errorMessage = messageSource.getMessage("server.error", null, locale);

        // Good practice: Log the actual exception so you can see it in the console
        ex.printStackTrace();

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("errorCode", "SERVER_ERROR");
        body.put("message", errorMessage);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
