package ru.practicum.ewmservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({IllegalStateException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequestResponseHandler(final Exception e) {
        ApiError apiError = new ApiError();
        apiError.addError(e.getClass().getName());
        apiError.setMessage(e.getLocalizedMessage());
        apiError.setReason(e.getMessage());
        apiError.setStatus(HttpStatus.BAD_REQUEST);
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationExceptionHandler(final MethodArgumentNotValidException e) {
        ApiError apiError = new ApiError();
        apiError.setErrors(e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toSet()));
        apiError.setMessage(e.getLocalizedMessage());
        apiError.setReason(e.getMessage());
        apiError.setStatus(HttpStatus.BAD_REQUEST);
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFoundExceptionHandler(final EntityNotFoundException e) {
        ApiError apiError = new ApiError();
        apiError.addError(e.getClass().getName());
        apiError.setMessage(e.getLocalizedMessage());
        apiError.setReason(e.getMessage());
        apiError.setStatus(HttpStatus.NOT_FOUND);
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflictExceptionHandler(final DataConflictException e) {
        ApiError apiError = new ApiError();
        apiError.addError(e.getClass().getName());
        apiError.setMessage(e.getLocalizedMessage());
        apiError.setReason(e.getMessage());
        apiError.setStatus(HttpStatus.CONFLICT);
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }
}