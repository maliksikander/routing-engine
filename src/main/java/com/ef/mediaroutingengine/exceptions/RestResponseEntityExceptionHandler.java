package com.ef.mediaroutingengine.exceptions;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import javax.naming.AuthenticationException;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;


@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestResponseEntityExceptionHandler extends  BaseExceptionHandler{

    public  RestResponseEntityExceptionHandler(){
        super();
    }
    @Autowired
    public  RestResponseEntityExceptionHandler(MessageSource messageSource){
        this.messageSource =messageSource;
    }


    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        List<String> errors = new ArrayList<>();

        for(ObjectError error : ex.getAllErrors()){

            String  errorMessage = error.getDefaultMessage() !=null ?
                    getTranslatedMessage(error.getDefaultMessage(),null):
                    getTranslatedMessage(error);
            String fullErrorMsg = error.getObjectName() + ":" + errorMessage;

            logger.warn(fullErrorMsg);
            errors.add(fullErrorMsg);
        }

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getTranslatedMessage("error.validation",null));
        apiError.setErrors(errors);
        return  buildResponseEntity(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {


        List<String> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()){
            String code = error.getCode();
            Object [] args = error.getArguments();
            String errorMessage = code !=null ? getTranslatedMessage(code,args) : error.getDefaultMessage();
            errors.add(error.getField() + ":" + errorMessage);


        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {

            String errorMessage = error.getDefaultMessage() != null?
                    getTranslatedMessage(error.getDefaultMessage(),null):
                    getTranslatedMessage(error);
            errors.add(errorMessage);



        }
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getTranslatedMessage("error.validation",null));
        apiError.setErrors(errors);
        logger.error(ex.getStackTrace());
        return  buildResponseEntity(apiError);
    }

    @ExceptionHandler({AccessDeniedException.class})
    protected ResponseEntity<Object> handleAccessDeniedException(final  AccessDeniedException ex){

        String errorMessage = "error.access.denied";

        if(!errorMessage.equals(ex.getMessage()) && !ex.getMessage().equals(getTranslatedMessage(errorMessage,null))){
            errorMessage = ex.getMessage();
        }
        logger.error(ex.getStackTrace());
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, getTranslatedMessage(errorMessage,null));
        return  buildResponseEntity(apiError);
    }

    @ExceptionHandler({AuthenticationException.class})
    protected  ResponseEntity<Object> handleAuthenticationException(final  AuthenticationException ex){

        logger.error(ex.getStackTrace());
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED,getTranslatedMessage(ex.getMessage(),null));
        return  new ResponseEntity<>(error,error.getStatus());
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(final  NotFoundException ex) {
        logger.error(ex.getMessage());
        ApiError error = new ApiError(HttpStatus.NOT_FOUND,getTranslatedMessage(ex.getMessage(),null));
        return  new ResponseEntity<>(error,error.getStatus());
    }

    @ExceptionHandler({MalformedURLException.class})
    public ResponseEntity<Object> handleMalformedURLException(final  MalformedURLException ex) {
        logger.error(ex.getMessage());
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST,getTranslatedMessage(ex.getMessage(),null));
        return  new ResponseEntity<>(error,error.getStatus());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgumentException(final  IllegalArgumentException ex) {
        logger.error(ex.getMessage());
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST,getTranslatedMessage(ex.getMessage(),null));
        return  new ResponseEntity<>(error,error.getStatus());
    }

    @ExceptionHandler({JSONException.class})
    public ResponseEntity<Object> handleJsonException(final JSONException ex) {
        logger.error(ex.getMessage());
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST,getTranslatedMessage(ex.getMessage(),null));
        return  new ResponseEntity<>(error,error.getStatus());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object>  constraintViolationException(Exception ex, WebRequest request) throws IOException {
        logger.error(ex.getMessage());
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST,getTranslatedMessage(ex.getMessage(),null));
        return  new ResponseEntity<>(error,error.getStatus());

    }


}
