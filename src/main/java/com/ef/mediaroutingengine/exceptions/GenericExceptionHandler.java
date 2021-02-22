package com.ef.mediaroutingengine.exceptions;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GenericExceptionHandler extends BaseExceptionHandler {

    public GenericExceptionHandler() {
        super();
    }

    @Autowired
    public GenericExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<Object> handleGenericException(final Exception ex) {

        logger.error(
                String.format("Uncaught error. Stacktrace is : %s", ex + getFullStackTraceLog(ex)));
        ErrorResponseBody errorResponseBody = new ErrorResponseBody(
                getTranslatedMessage("error.generic.internal.server.error", null), ex.getMessage());
        return buildResponseEntity(errorResponseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getFullStackTraceLog(Exception ex) {
        return Arrays.asList(ex.getStackTrace()).stream().map(Objects::toString)
                .collect(Collectors.joining());
    }

}
