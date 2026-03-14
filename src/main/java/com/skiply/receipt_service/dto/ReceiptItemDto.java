package com.skiply.receipt_service.dto;

public record ReceiptItemDto (
        String purchaseItem,
        Integer quantity,
        Double unitPrice
) {

}
