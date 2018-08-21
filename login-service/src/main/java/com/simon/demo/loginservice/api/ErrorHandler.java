package com.simon.demo.loginservice.api;

import com.simon.demo.loginservice.model.ErrorDTO;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
@ResponseBody
public class ErrorHandler {
    @ExceptionHandler(feign.RetryableException.class)
    public ErrorDTO handleRetryableException(Exception e) {
        System.out.println("--------RetryableException----------");
        e.printStackTrace();
        return new ErrorDTO();
    }

    @ExceptionHandler(RestClientException.class)
    public ErrorDTO handleRestClientException(Exception e) {
        System.out.println("--------RestClientException----------");
        e.printStackTrace();
        return new ErrorDTO();
    }
}
