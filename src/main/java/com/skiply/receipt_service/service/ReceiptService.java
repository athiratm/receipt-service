package com.skiply.receipt_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.skiply.receipt_service.repository.ReceiptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptGeneratorService receiptGeneratorService;
    private final FeeClient feeClient;
    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

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

    @Transactional
    private Resource generateNewReceipt(Long transactionId) {

        logger.warn("Receipt not found.");
        logger.warn("Generating for {}", transactionId);

        FeeResponse fee = feeClient.getFeeByTransactionId(transactionId);

        logger.warn("Fee found. Amnt  {}", fee.totalAmount());

        CreateReceiptRequest request = mapToReceiptRequest(fee);

        ReceiptResponse receipt = createReceipt(request);

      //  receiptGeneratorService.generatePdf(request, receipt.receiptNumber());

        Resource resource =  new FileSystemResource(receipt.receiptPath());

        if (!resource.exists()) {
            throw new RuntimeException(
                    "Generated receipt file not found for transaction " + transactionId);
        }

        return resource;
    }

    public ReceiptResponse createReceipt(CreateReceiptRequest request) {

        Receipt receipt = new Receipt();
        receipt.setTransactionId(request.transactionId());

        receiptRepository.saveAndFlush(receipt);

        String receiptNumber = "REC-" + receipt.getReceiptId();
        String path = "receipts/" + receiptNumber + ".pdf";

        receipt.setReceiptNumber(receiptNumber);
        receipt.setReceiptPath(path);

        receipt = receiptRepository.save(receipt);

        receiptGeneratorService.generatePdf(request, receiptNumber);

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