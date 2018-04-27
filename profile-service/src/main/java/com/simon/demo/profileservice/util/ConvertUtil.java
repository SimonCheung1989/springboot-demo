package com.simon.demo.profileservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConvertUtil.class);

    public static <T> T convertJavaBean2DestinationJavaBean(Object obj, Class<T> clasz) {
        T result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.convertValue(obj, clasz);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return result;

    }
}
