package com.skiply.receipt_service.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skiply.receipt_service.dto.CreateReceiptRequest;
import com.skiply.receipt_service.dto.ReceiptResponse;
import com.skiply.receipt_service.service.ReceiptService;

import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/receipts")
@RequiredArgsConstructor
@Validated
public class ReceiptController {

    private final ReceiptService receiptService;

    @Operation(summary = "Create a new receipt", description = "Creates a new receipt based on the provided transaction details.")
    @PostMapping
    public ResponseEntity<ReceiptResponse> createReceipt(@Valid @RequestBody CreateReceiptRequest request) {

        ReceiptResponse receiptResponse = receiptService.createReceipt(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(receiptResponse);
    }

    @Operation(summary = "Download a receipt", description = "Downloads the receipt associated with the given transaction ID. If the receipt doesn't exist, it will be generated.")
    @GetMapping("/{transactionId}/download")
    public ResponseEntity<Resource> downloadReceipt(
            @PathVariable @Positive(message = "TransactionId must be positive") Long transactionId) {

        Resource receipt =
                receiptService.getOrGenerateReceipt(transactionId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"receipt-" + transactionId + ".pdf\"")
                .body(receipt);
    }
}
