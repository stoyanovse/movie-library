package org.alphatrack.movielibrary.exceptions;


import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.alphatrack.movielibrary.dtos.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    public static final String NOT_FOUND = "Not Found";
    public static final String CONFLICT = "Conflict";
    public static final String FORBIDDEN = "Forbidden";
    public static final String BAD_REQUEST = "Bad Request";

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFoundException(EntityNotFoundException e, HttpServletRequest path) {

        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .error(NOT_FOUND)
                .message(e.getMessage())
                .path(path.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEntityException(EntityExistsException e, HttpServletRequest path) {

        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .error(CONFLICT)
                .message(e.getMessage())
                .path(path.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error,HttpStatus.CONFLICT);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleSpringSecurityAccessDeniedException(AccessDeniedException e,HttpServletRequest path){

        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .error(FORBIDDEN)
                .message(e.getMessage())
                .path(path.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error,HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest path) {

        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(BAD_REQUEST)
                .message(e.getMessage())
                .path(path.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}

