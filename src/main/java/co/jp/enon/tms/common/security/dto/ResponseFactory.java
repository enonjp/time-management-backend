package co.jp.enon.tms.common.security.dto;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import jakarta.validation.ValidationException;

import co.jp.enon.tms.common.exception.NotFoundItemException;

public final class ResponseFactory {

    private ResponseFactory() {
        // prevent instantiation
    }

    /* SUCCESS */
    public static SuccessResponse success(Object data) {
        SuccessResponse response = new SuccessResponse();
        response.setResult(data);
        return response;
    }

    /* ERROR */
    public static ErrorResponse error(
            Exception ex,
            String path
    ) {
        ErrorResponse error = new ErrorResponse();

        if (ex instanceof ValidationException) {
            error.setStatus(new Status("BAD_REQUEST", "Validation failed"));
            error.setError("VALIDATION_ERROR");
            error.setMessage(ex.getMessage());

        } else if (ex instanceof AuthenticationException) {
            error.setStatus(new Status("UNAUTHORIZED", "Authentication failed"));
            error.setError("AUTHENTICATION_FAILED");
            error.setMessage("Incorrect email or password");

        } else if (ex instanceof AccessDeniedException) {
            error.setStatus(new Status("FORBIDDEN", "Access denied"));
            error.setError("ACCESS_DENIED");
            error.setMessage("No permission to access this resource");

        } else if (ex instanceof NotFoundItemException) {
            error.setStatus(new Status("NOT_FOUND", "Resource not found"));
            error.setError("NOT_FOUND");
            error.setMessage(ex.getMessage());

        } else if (ex instanceof DuplicateKeyException) {
            error.setStatus(new Status("CONFLICT", "Email already exists"));
            error.setError("DUPLICATE_RESOURCE");
            error.setMessage("Email already exists");
            
    	}else {
            error.setStatus(new Status("INTERNAL_SERVER_ERROR", "Unexpected error"));
            error.setError(ex.getClass().getSimpleName());
            error.setMessage(ex.getMessage());
        }

        error.setPath(path);
        return error;
    }

    /* OVERLOAD (optional) */

    public static ErrorResponse error(
            Exception ex,
            HttpServletRequest request
    ) {
        return error(ex, request.getRequestURI());
    }
}

