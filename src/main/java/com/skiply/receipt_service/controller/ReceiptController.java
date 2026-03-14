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

import lombok.RequiredArgsConstructor;

/**
 * REST controller for create and download receipts for fee payment.
 */
@RestController
@RequestMapping("/api/v1/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /**
     * Creates a new receipt based on the provided transaction details.
     *
     * @param request the receipt creation details
     * @return the created receipt response
     */
    @PostMapping
    public ResponseEntity<ReceiptResponse> createReceipt(@RequestBody CreateReceiptRequest request) {

        ReceiptResponse receiptResponse = receiptService.createReceipt(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(receiptResponse);
    }

    /**
     * Downloads the receipt associated with the given transaction ID.
     * If the receipt doesn't exist, it will be generated.
     *
     * @param transactionId the unique identifier of the transaction
     * @return a response entity containing the PDF resource
     */
    @GetMapping("/{transactionId}/download")
    public ResponseEntity<Resource> downloadReceipt(
            @PathVariable Long transactionId) {

        Resource receipt =
                receiptService.getOrGenerateReceipt(transactionId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"receipt-" + transactionId + ".pdf\"")
                .body(receipt);
    }
}
