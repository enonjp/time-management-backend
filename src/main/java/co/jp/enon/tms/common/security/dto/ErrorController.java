package co.jp.enon.tms.common.security.dto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.jp.enon.tms.common.exception.NotFoundItemException;

@RestControllerAdvice
public class ErrorController {

    private static final Logger log =
            LoggerFactory.getLogger(ErrorController.class);

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleValidationException(
            HttpServletRequest request,
            ValidationException ex) {

        log.warn("Validation error: {}", ex.getMessage());
        return ResponseFactory.error(ex, request.getRequestURI());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleIllegalArgument(
            HttpServletRequest request,
            IllegalArgumentException ex) {

        return ResponseFactory.error(ex, request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response handleAuthenticationException(
            HttpServletRequest request,
            AuthenticationException ex) {

        log.warn("Authentication error: {}", ex.getMessage());
        return ResponseFactory.error(ex, request.getRequestURI());
    }

    @ExceptionHandler(NotFoundItemException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handleNotFoundItemException(
            HttpServletRequest request,
            NotFoundItemException ex) {

        return ResponseFactory.error(ex, request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED) // ✅ 405, not 403
    public Response handleMethodNotSupported(
            HttpServletRequest request,
            HttpRequestMethodNotSupportedException ex) {

        return ResponseFactory.error(ex, request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Response handleAccessDenied(
            HttpServletRequest request,
            AccessDeniedException ex) {

        return ResponseFactory.error(ex, request.getRequestURI());
    }
    
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public Response handleDuplicateKey(
            HttpServletRequest request,
            DuplicateKeyException ex) {

        log.warn("Duplicate key error: {}", ex.getMessage());
        return ResponseFactory.error(ex, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleGeneric(
            HttpServletRequest request,
            Exception ex) {

        log.error("Unhandled exception", ex);
        return ResponseFactory.error(ex, request.getRequestURI());
    }
}

