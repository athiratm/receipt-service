package com.skiply.receipt_service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateReceiptRequest (
        @NotNull(message = "Student Id is required")
        @Min(value = 1, message = "Student ID must be a positive number")
        Long transactionId,

        @NotBlank(message = "Student Name is required")
        String studentName,

        @NotBlank(message = "School Name is required")
        String schoolName,

        @Positive(message = "Unit price must be greater than zero")
        Double totalAmount,

        @NotNull(message = "Payment date is required")
        LocalDateTime paymentDate,

        @NotBlank
        String referenceNumber,

        @NotBlank(message = "Card number is required")
        @Size(min = 16, max = 16, message = "Invalid card number")
        String cardNumber,

        @NotBlank(message = "Card type is required")
        String cardType,

        @NotEmpty(message = "At least one purchase item is required")
        List<ReceiptItemDto> receiptItems
) implements Serializable {}
