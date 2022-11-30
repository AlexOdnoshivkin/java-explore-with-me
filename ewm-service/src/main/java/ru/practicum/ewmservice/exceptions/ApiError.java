package ru.practicum.ewmservice.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class ApiError {

    private Set<String> errors = new HashSet<>();

    private String message;

    private String reason;

    private HttpStatus status;

    private LocalDateTime timestamp;


    public ApiError() {
    }

    public void addError(String error) {

        errors.add(error);
    }
}
