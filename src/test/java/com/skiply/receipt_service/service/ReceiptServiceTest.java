package com.skiply.receipt_service.service;

import com.skiply.receipt_service.client.FeeClient;
import com.skiply.receipt_service.dto.CreateReceiptRequest;
import com.skiply.receipt_service.dto.FeeResponse;
import com.skiply.receipt_service.dto.PurchaseDetailResponse;
import com.skiply.receipt_service.dto.ReceiptItemDto;
import com.skiply.receipt_service.dto.ReceiptResponse;
import com.skiply.receipt_service.entity.Receipt;
import com.skiply.receipt_service.exception.ReceiptGenerationException;
import com.skiply.receipt_service.repository.ReceiptRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReceiptService}.
 * Tests the business logic for creating and retrieving receipts.
 */
@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private ReceiptGeneratorService receiptGeneratorService;

    @Mock
    private FeeClient feeClient;

    @InjectMocks
    private ReceiptService receiptService;

    private Receipt existingReceipt;

    /**
     * Initializes test data and mock configurations before each test.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(receiptService, "receiptStoragePath", "target/receipts");
        
        existingReceipt = new Receipt();
        existingReceipt.setReceiptId(1L);
        existingReceipt.setTransactionId(1L);
        existingReceipt.setReceiptNumber("REC-1");
        existingReceipt.setReceiptPath("target/receipts/REC-1.pdf");
    }

    /**
     * Tests that the service returns an existing receipt from the database.
     */
    @Test
    void getOrGenerateReceipt_whenReceiptExists_shouldReturnExistingReceipt() {

        when(receiptRepository.findByTransactionId(1L))
                .thenReturn(Optional.of(existingReceipt));

        Resource result = receiptService.getOrGenerateReceipt(1L);

        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo("REC-1.pdf");

        verify(receiptRepository).findByTransactionId(1L);
        verifyNoInteractions(feeClient);
        verifyNoInteractions(receiptGeneratorService);
    }

    /**
     * Tests that the service generates a new receipt when one doesn't exist in the database.
     */
    @Test
    void getOrGenerateReceipt_whenReceiptNotExists_shouldGenerateNewReceipt() throws IOException {

        when(receiptRepository.findByTransactionId(1L))
                .thenReturn(Optional.empty());

        FeeResponse fee = new FeeResponse(
                1L,
                1L,
                500.0,
                "CREATED",
                "REF-123",
                "XXXX-XXXX-1234",
                LocalDateTime.now(),
                List.of(new PurchaseDetailResponse(1L, "Test Fee", 1, 500.0, 500.0 ))
        );

        when(feeClient.getFeeByTransactionId(1L))
                .thenReturn(fee);

        Receipt receipt = new Receipt();
        receipt.setReceiptId(1L);
        receipt.setTransactionId(1L);

        // Mock first save during createReceipt
        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        // Create dummy file for existence check
        Path path = Path.of("target/receipts/REC-1.pdf");
        Files.createDirectories(path.getParent());
        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        Resource result = receiptService.getOrGenerateReceipt(1L);

        assertThat(result).isNotNull();
        assertThat(result.exists()).isTrue();

        verify(feeClient).getFeeByTransactionId(1L);
        verify(receiptGeneratorService).generatePdf(any(CreateReceiptRequest.class), eq("REC-1"));
        
        // Cleanup dummy file
        Files.deleteIfExists(path);
    }

    /**
     * Tests the creation and persistence of a new receipt record.
     */
    @Test
    void createReceipt_shouldPersistReceiptAndReturnResponse() {

        CreateReceiptRequest request = new CreateReceiptRequest(
                1L,
                "Student",
                "School",
                500.0,
                LocalDateTime.now(),
                "REF-1",
                "1234567812345678",
                "Master",
                List.of(new ReceiptItemDto("Test Fee", 1, 500.0))
        );

        Receipt receipt = new Receipt();
        receipt.setReceiptId(1L);
        receipt.setTransactionId(1L);

        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        ReceiptResponse response = receiptService.createReceipt(request);

        assertThat(response.receiptId()).isEqualTo(1L);
        assertThat(response.receiptNumber()).isEqualTo("REC-1");
        assertThat(response.receiptPath()).contains("REC-1.pdf");

        // Service saves twice (initial and with details)
        verify(receiptRepository, times(2)).save(any(Receipt.class));
    }

    /**
     * Tests that the service throws an exception when a newly generated receipt file is missing.
     */
    @Test
    void getOrGenerateReceipt_whenGeneratedFileMissing_shouldThrowException() {

        when(receiptRepository.findByTransactionId(1L))
                .thenReturn(Optional.empty());

        FeeResponse fee = new FeeResponse(
                1L,
                1L,
                500.0,
                "CREATED",
                "REF-123",
                "XXXX-XXXX-1234",
                LocalDateTime.now(),
                List.of(new PurchaseDetailResponse(1L, "Test Fee", 1, 500.0, 500.0 ))
        );

        when(feeClient.getFeeByTransactionId(1L))
                .thenReturn(fee);

        Receipt receipt = new Receipt();
        receipt.setReceiptId(1L);
        receipt.setTransactionId(1L);

        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        // Ensure dummy file is deleted
        File file = new File("target/receipts/REC-1.pdf");
        if (file.exists()) {
            file.delete();
        }

        assertThatThrownBy(() ->
                receiptService.getOrGenerateReceipt(1L))
                .isInstanceOf(ReceiptGenerationException.class)
                .hasMessageContaining("Generated receipt file not found");
    }
}
