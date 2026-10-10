package com.avnish.descriptAI_backend.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * @author Avnish
 * @date 10-10-2026
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Uniform JSON body for every error. */
    public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    // ---- Authentication --------------------------------------------------

    /**
     * DaoAuthenticationProvider converts UsernameNotFoundException into
     * BadCredentialsException (hideUserNotFoundExceptions = true), so a wrong
     * username or password from authenticationManager.authenticate() arrives here.
     * UsernameNotFoundException is kept as well in case you call
     * loadUserByUsername() directly. One message for both, so attackers cannot
     * tell which part was wrong.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(Exception ex, HttpServletRequest request) {
        log.warn("Login failed: {}", ex.getClass().getSimpleName());
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", request);
    }



    /** Any other authentication failure thrown from inside a controller/service. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Authentication failed", request);
    }

    // ---- Authorization ---------------------------------------------------

    /**
     * Only fires for method-level security (@PreAuthorize etc.). URL-rule denials
     * from your SecurityFilterChain never reach a @ControllerAdvice.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on {}", request.getRequestURI());
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request);
    }

    // ---- Request problems ------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }


    // ---- Fallback --------------------------------------------------------

    /** Last resort: log the full stack trace, return nothing sensitive. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.", request);
    }
}