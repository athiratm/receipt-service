package com.skiply.receipt_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FeeResponse(
        Long transactionId,
        Long studentId,
        Double totalAmount,
        String status,
        String referenceNumber,
        String maskedCardNumber,
        LocalDateTime paymentDate,
        List<PurchaseDetailResponse> items) {
}