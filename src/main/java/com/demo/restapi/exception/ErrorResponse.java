package com.demo.restapi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String errorCode; // e.g., "RESOURCE_NOT_FOUND"
    private String message;
    private LocalDateTime timestamp;
}