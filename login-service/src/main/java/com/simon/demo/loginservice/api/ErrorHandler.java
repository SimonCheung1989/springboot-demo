package com.simon.demo.loginservice.api;

import com.simon.demo.loginservice.model.ErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
@ResponseBody
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(feign.RetryableException.class)
    public ErrorDTO handleRetryableException(Exception e) {
        log.debug("--------RetryableException----------");
        log.error(e.getMessage(), e);
        return new ErrorDTO();
    }

    @ExceptionHandler(RestClientException.class)
    public ErrorDTO handleRestClientException(Exception e) {
        log.debug("--------RestClientException----------");
        log.error(e.getMessage(), e);
        return new ErrorDTO();
    }
}
