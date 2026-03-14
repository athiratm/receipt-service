package com.skiply.receipt_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skiply.receipt_service.client.FeeClient;
import com.skiply.receipt_service.dto.CreateReceiptRequest;
import com.skiply.receipt_service.dto.FeeResponse;
import com.skiply.receipt_service.dto.ReceiptItemDto;
import com.skiply.receipt_service.dto.ReceiptResponse;
import com.skiply.receipt_service.entity.Receipt;
import com.skiply.receipt_service.exception.ReceiptGenerationException;
import com.skiply.receipt_service.exception.ResourceNotFoundException;
import com.skiply.receipt_service.repository.ReceiptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptGeneratorService receiptGeneratorService;
    private final FeeClient feeClient;
    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

    @Value("${receipt.storage-path}")
    private String receiptStoragePath;

    @Transactional
    public Resource getOrGenerateReceipt(Long transactionId) {

        return receiptRepository.findByTransactionId(transactionId)
                .map(this::getExistingReceipt)
                .orElseGet(() -> generateNewReceipt(transactionId));
    }

    private Resource  getExistingReceipt(Receipt receipt) {

        logger.info("Receipt already exists for transaction {}", receipt.getTransactionId());

        return new FileSystemResource(receipt.getReceiptPath());
    }

    private Resource generateNewReceipt(Long transactionId) {

        logger.info("Receipt not found for transaction {}. Generating new receipt.", transactionId);

        FeeResponse fee = feeClient.getFeeByTransactionId(transactionId);
        if (fee == null) {
            throw new ResourceNotFoundException(
                    "Data not found for transaction " + transactionId);
        }

        logger.warn("Fee found. Amnt  {}", fee.totalAmount());

        CreateReceiptRequest request = mapToReceiptRequest(fee);

        ReceiptResponse receipt = createReceipt(request);
        if (receipt == null || receipt.receiptPath() == null || receipt.receiptPath().isBlank()) {
            throw new ReceiptGenerationException(
                    "Invalid receipt path generated for transaction " + transactionId);
        }

        Resource resource =  new FileSystemResource(receipt.receiptPath());

        if (!resource.exists()) {
            throw new ReceiptGenerationException(
                    "Generated receipt file not found for transaction " + transactionId);
        }

        return resource;
    }

    @Transactional
    public ReceiptResponse createReceipt(CreateReceiptRequest request) {
        logger.info("Creating receipt for transaction {}", request.transactionId());

        Receipt receipt = new Receipt();
        receipt.setTransactionId(request.transactionId());

        receiptRepository.save(receipt);

        String receiptNumber = "REC-" + receipt.getReceiptId();
        String path = receiptStoragePath + "/" + receiptNumber + ".pdf";

        receipt.setReceiptNumber(receiptNumber);
        receipt.setReceiptPath(path);

        receipt = receiptRepository.save(receipt);

        try {
            receiptGeneratorService.generatePdf(request, receiptNumber);
        } catch (Exception ex) {
            throw new ReceiptGenerationException(
                    "Failed to generate PDF for receipt " + receiptNumber);
        }

        return new ReceiptResponse(
                receipt.getReceiptId(),
                receipt.getReceiptNumber(),
                receipt.getReceiptPath());
    }

    private CreateReceiptRequest mapToReceiptRequest(FeeResponse fee) {

        List<ReceiptItemDto> items =
                fee.items()
                        .stream()
                        .map(i -> new ReceiptItemDto(
                                i.purchaseItem(),
                                i.quantity(),
                                i.unitPrice()))
                        .toList();

        return new CreateReceiptRequest(
                fee.transactionId(),
                "StudentName",
                "SchoolName",
                fee.totalAmount(),
                fee.paymentDate(),
                fee.referenceNumber(),
                fee.maskedCardNumber(),
                "CardType",
                items
        );
    }
}
