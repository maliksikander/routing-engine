package com.ef.mediaroutingengine.exceptions;

import java.net.MalformedURLException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestResponseEntityExceptionHandler extends BaseExceptionHandler {

    public RestResponseEntityExceptionHandler() {
        super();
    }

    @Autowired
    public RestResponseEntityExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
//        List<String> errors = new ArrayList<>();
//        for (FieldError error : ex.getBindingResult().getFieldErrors()){
//            String code = error.getCode();
//            Object [] args = error.getArguments();
////            String errorMessage = code !=null ? getTranslatedMessage(code,args) : error.getDefaultMessage();
//            errors.add("Error on field '" + error.getField() + "': " + error.getDefaultMessage());
//        }
//        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
//
//            String errorMessage = error.getDefaultMessage() != null?
//                    getTranslatedMessage(error.getDefaultMessage(),null):
//                    getTranslatedMessage(error);
//            errors.add(errorMessage);
//        }

        String message = null;
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            FieldError fieldError = fieldErrors.get(0);
            message = "field '" + fieldError.getField() + "': " + fieldError.getDefaultMessage();
        }

        ErrorResponseBody errorResponseBody = new ErrorResponseBody("error.validation", message);
        logger.error(ex.getStackTrace());

        return buildResponseEntity(errorResponseBody, HttpStatus.BAD_REQUEST);
    }

    @NotNull
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, @NotNull HttpHeaders headers,
            @NotNull HttpStatus status, @NotNull WebRequest request) {
        String error = "error.invalid-field-format";
        ErrorResponseBody responseBody = new ErrorResponseBody(error, ex.getMessage());
        return buildResponseEntity(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        String error = "error.method-argument-type-mismatch";
        ErrorResponseBody responseBody = new ErrorResponseBody(error, ex.getMessage());
        return buildResponseEntity(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AccessDeniedException.class})
    protected ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException ex) {

        String errorMessage = "error.access.denied";

        if (!errorMessage.equals(ex.getMessage()) && !ex.getMessage()
                .equals(getTranslatedMessage(errorMessage, null))) {
            errorMessage = ex.getMessage();
        }
        logger.error(ex.getStackTrace());
        ErrorResponseBody errorResponseBody = new ErrorResponseBody(
                getTranslatedMessage(errorMessage, null), null);
        return buildResponseEntity(errorResponseBody, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({AuthenticationException.class})
    protected ResponseEntity<Object> handleAuthenticationException(
            final AuthenticationException ex) {

        logger.error(ex.getStackTrace());
        ErrorResponseBody error = new ErrorResponseBody(getTranslatedMessage(ex.getMessage(), null),
                null);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles not-found exception.
     *
     * @param ex NotFoundException
     * @return ResponseEntity
     */
    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(final NotFoundException ex) {
        logger.error(ex.getMessage());
        String error = "error.resource-not-found";
        ErrorResponseBody responseBody = new ErrorResponseBody(error, ex.getMessage());
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles Malformed url exception.
     *
     * @param ex MalformedURLException object
     * @return ResponseEntity object
     */
    @ExceptionHandler({MalformedURLException.class})
    public ResponseEntity<Object> handleMalformedURLException(final MalformedURLException ex) {
        logger.error(ex.getMessage());
        ErrorResponseBody error = new ErrorResponseBody(getTranslatedMessage(ex.getMessage(), null),
                null);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Illegal argument exception.
     *
     * @param ex IllegalArgumentException object
     * @return ResponseEntity object
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgumentException(
            final IllegalArgumentException ex) {
        logger.error(ex.getMessage());
        String error = "error.illegal-argument(s)";
        ErrorResponseBody responseBody = new ErrorResponseBody(getTranslatedMessage(error, null),
                ex.getMessage());
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles JSON Exception.
     *
     * @param ex JSONException object
     * @return ResponseEntity object
     */
    @ExceptionHandler({JSONException.class})
    public ResponseEntity<Object> handleJsonException(final JSONException ex) {
        logger.error(ex.getMessage());
        ErrorResponseBody error = new ErrorResponseBody(getTranslatedMessage(ex.getMessage(), null),
                null);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Constraint Violation Exception.
     *
     * @param ex ConstraintViolationException object
     * @return ResponseEntity object
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> constraintViolationException(Exception ex, WebRequest request) {
        logger.error(ex.getMessage());
        ErrorResponseBody error = new ErrorResponseBody(getTranslatedMessage(ex.getMessage(), null),
                null);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
