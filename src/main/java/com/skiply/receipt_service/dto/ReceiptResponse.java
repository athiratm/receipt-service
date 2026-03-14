package com.skiply.receipt_service.dto;

public record ReceiptResponse(
        Long receiptId,
        String receiptNumber,
        String receiptPath
) {}
