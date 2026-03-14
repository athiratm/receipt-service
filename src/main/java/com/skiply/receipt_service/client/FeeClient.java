package com.skiply.receipt_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.skiply.receipt_service.dto.FeeResponse;

@FeignClient(
        name = "fee-service",
        path = "/api/v1/fees",
        fallbackFactory = FeeClientFallbackFactory.class
)
public interface FeeClient {
    @GetMapping("/transactions/{id}")
    FeeResponse getFeeByTransactionId(@PathVariable("id") Long id);
}
