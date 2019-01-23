package com.bugtracking.server.web;

import com.bugtracking.server.domain.exceptions.InvalidRequestException;
import com.bugtracking.server.domain.exceptions.ObjectAlreadyExistsException;
import com.bugtracking.server.domain.exceptions.ObjectNotFoundException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice(annotations = RestController.class)
public class ExceptionHandlingAdvice {

    @ExceptionHandler(ObjectNotFoundException.class)
    public String handleException(ObjectNotFoundException ex, HttpServletRequest request) {
        request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, HttpStatus.NOT_FOUND.value());
        return "forward:/error";
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    public String handleException(ObjectAlreadyExistsException ex, HttpServletRequest request) {
        request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, HttpStatus.CONFLICT.value());
        return "forward:/error";
    }

    @ExceptionHandler(InvalidRequestException.class)
    public String handleException(InvalidRequestException ex, HttpServletRequest request) {
        request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, HttpStatus.BAD_REQUEST.value());
        return "forward:/error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        StringBuilder errorMessageBuilder = new StringBuilder("Validation failed for argument '");
        errorMessageBuilder.append(ex.getName()).append("'. ");
        errorMessageBuilder.append("Failed to convert value \"");
        errorMessageBuilder.append(ex.getValue()).append("\" to target type.");

        request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, HttpStatus.BAD_REQUEST.value());
        request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex);
        request.setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, errorMessageBuilder.toString());

        return "forward:/error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        StringBuilder errorMessageBuilder = new StringBuilder("Validation failed.");
        if (ex.getBindingResult() != null) {
            BindingResult bindingResult = ex.getBindingResult();
            if (bindingResult.getFieldError() != null) {
                errorMessageBuilder.append(" Field '");
                errorMessageBuilder.append(bindingResult.getFieldError().getField());
                if (bindingResult.getFieldError().getDefaultMessage() != null) {
                    errorMessageBuilder.append("' ");
                    errorMessageBuilder.append(bindingResult.getFieldError().getDefaultMessage());
                }
            }
        }
        errorMessageBuilder.append('.');

        request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, HttpStatus.BAD_REQUEST.value());
        request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex);
        request.setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, errorMessageBuilder.toString());

        return "forward:/error";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleException(ConstraintViolationException ex, HttpServletRequest request) {
        StringBuilder errorMessageBuilder = new StringBuilder("Validation failed.");
        for (ConstraintViolation constraintViolation : ex.getConstraintViolations()) {
            errorMessageBuilder.append(' ');
            errorMessageBuilder.append(constraintViolation.getMessage());
        }

        request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, HttpStatus.BAD_REQUEST.value());
        request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex);
        request.setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, errorMessageBuilder.toString());

        return "forward:/error";
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        errorMessageBuilder.append("Can not parse request body.");

        if (ex.getCause() != null && ex.getCause() instanceof InvalidFormatException) {
            @SuppressWarnings("unchecked")
            InvalidFormatException formatException = (InvalidFormatException) ex.getCause();
            for (JsonMappingException.Reference reference : formatException.getPath()) {
                if (reference.getFieldName() != null) {
                    errorMessageBuilder.append(" Invalid value for field '" + reference.getFieldName() + "'");
                    String messageToMatch = formatException.getMessage().split("\n")[0];
                    Matcher matcher = Pattern.compile("^Can not[^:]*: (.+)$").matcher(messageToMatch);
                    if (matcher.matches()) {
                        errorMessageBuilder.append(" : ").append(matcher.replaceAll("$1"));
                    }
                    if (errorMessageBuilder.charAt(errorMessageBuilder.length() - 1) != '.') {
                        errorMessageBuilder.append('.');
                    }
                }
            }
        }

        request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, HttpStatus.BAD_REQUEST.value());
        request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex);
        request.setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, errorMessageBuilder.toString());

        return "forward:/error";
    }
}
