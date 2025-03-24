package com.kdu.rizzlers.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

abstract class ApiSubError {
}

@Data
@AllArgsConstructor
class ApiValidationError extends ApiSubError {
    private String object;
    private String field;
    private Object rejectedValue;
    private String message;

    ApiValidationError(String object, String message) {
        this.object = object;
        this.message = message;
    }
} 