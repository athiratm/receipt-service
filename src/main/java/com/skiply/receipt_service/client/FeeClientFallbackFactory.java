package com.skiply.receipt_service.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.skiply.receipt_service.exception.ServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FeeClientFallbackFactory implements FallbackFactory<FeeClient> {
    @Override
    public FeeClient create(Throwable cause) {
        return id -> {

            log.error("Fee service failure cause: ", cause);
            throw new ServiceUnavailableException("Fee service is currently unavailable");
        };
    }

}
