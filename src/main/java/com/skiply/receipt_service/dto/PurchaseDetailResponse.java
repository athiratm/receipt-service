package com.skiply.receipt_service.dto;

public record PurchaseDetailResponse(
        Long id,
        String purchaseItem,
        Integer quantity,
        Double unitPrice,
        Double subTotal
) {}