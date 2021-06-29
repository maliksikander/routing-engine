package com.ef.mediaroutingengine.exceptions;

import java.util.Locale;
import javax.annotation.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

public class BaseExceptionHandler extends ResponseEntityExceptionHandler {

    protected MessageSource messageSource;

    public BaseExceptionHandler() {
        super();
    }

    /**
     * Build the response body in case of error.
     *
     * @param errorResponseBody ErrorResponseBody
     * @return ResponseEntity
     */
    protected ResponseEntity<Object> buildResponseEntity(ErrorResponseBody errorResponseBody,
                                                         HttpStatus httpStatus) {
        return new ResponseEntity<>(errorResponseBody, httpStatus);
    }

    /**
     * Retrieves translated error message.
     *
     * @param ex   exception
     * @param args list of objects
     * @return String
     */
    protected String getTranslatedMessage(Exception ex, Object[] args) {
        return getTranslatedMessage(ex.getMessage(), args);
    }

    /**
     * Retrieves translated error message.
     *
     * @param objectError ObjectError.
     * @return String
     */
    protected String getTranslatedMessage(ObjectError objectError) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(objectError, locale);
    }

    /**
     * Retrieves translated error message.
     *
     * @param message String
     * @param args    list of objects
     * @return String
     */
    protected String getTranslatedMessage(@Nullable String message, Object[] args) {

        if (message == null) {
            return null;
        }

        Locale locale = LocaleContextHolder.getLocale();
        String[] strArgs = null;
        if (args != null) {
            strArgs = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                strArgs[i] = args[i].toString();
            }
        }
        try {
            return messageSource.getMessage(message, strArgs, locale);
        } catch (NoSuchMessageException e) {
            return message;
        }

    }
}
