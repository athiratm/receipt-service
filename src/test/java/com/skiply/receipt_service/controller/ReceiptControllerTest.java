package com.skiply.receipt_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skiply.receipt_service.dto.CreateReceiptRequest;
import com.skiply.receipt_service.dto.ReceiptItemDto;
import com.skiply.receipt_service.dto.ReceiptResponse;
import com.skiply.receipt_service.service.ReceiptService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ReceiptController}.
 * Tests the REST API endpoints for creating and downloading receipts.
 */
@WebMvcTest(ReceiptController.class)
class ReceiptControllerTest {

    private static final String BASE_URL = "/api/v1/receipts";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReceiptService receiptService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Helper method to create a valid CreateReceiptRequest.
     */
    private CreateReceiptRequest validRequest() {
        ReceiptItemDto item = new ReceiptItemDto("Test Fee", 1, 500.0);

        return new CreateReceiptRequest(
                1L,
                "Test Student",
                "Test School",
                500.0,
                LocalDateTime.now(),
                "Ref-1",
                "1122334455667788",
                "Master",
                List.of(item)
        );
    }

    /**
     * Test case for successful receipt creation.
     */
    @Test
    void createReceipt_whenRequestValid_returnsCreatedReceipt() throws Exception {

        CreateReceiptRequest request = validRequest();

        ReceiptResponse response =
                new ReceiptResponse(1L, "REC-1", null);

        Mockito.when(receiptService.createReceipt(any()))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.receiptId").value(1L))
                .andExpect(jsonPath("$.receiptNumber").value("REC-1"));

        verify(receiptService).createReceipt(any());
    }

    /**
     * Test case for receipt creation with missing student name.
     */
    @Test
    void createReceipt_whenStudentNameMissing_returnsBadRequest() throws Exception {

        CreateReceiptRequest request = new CreateReceiptRequest(
                1L,
                "",
                "Test School",
                500.0,
                LocalDateTime.now(),
                "Ref-1",
                "1122334455667788",
                "Master",
                List.of(new ReceiptItemDto("Test Fee", 1, 500.0))
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(receiptService);
    }

    /**
     * Test case for receipt creation with an invalid card number.
     */
    @Test
    void createReceipt_whenCardNumberInvalid_returnsBadRequest() throws Exception {

        CreateReceiptRequest request = new CreateReceiptRequest(
                1L,
                "Test Student",
                "Test School",
                500.0,
                LocalDateTime.now(),
                "Ref-1",
                "1234",
                "Master",
                List.of(new ReceiptItemDto("Test Fee", 1, 500.0))
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(receiptService);
    }

    /**
     * Test case for receipt creation with a negative amount.
     */
    @Test
    void createReceipt_whenAmountNegative_returnsBadRequest() throws Exception {

        CreateReceiptRequest request = new CreateReceiptRequest(
                1L,
                "Test Student",
                "Test School",
                -100.0,
                LocalDateTime.now(),
                "Ref-1",
                "1122334455667788",
                "Master",
                List.of(new ReceiptItemDto("Test Fee", 1, 500.0))
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(receiptService);
    }

    /**
     * Test case for receipt creation with an empty items list.
     */
    @Test
    void createReceipt_whenReceiptItemsEmpty_returnsBadRequest() throws Exception {

        CreateReceiptRequest request = new CreateReceiptRequest(
                1L,
                "Test Student",
                "Test School",
                500.0,
                LocalDateTime.now(),
                "Ref-1",
                "1122334455667788",
                "Master",
                List.of()
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(receiptService);
    }

    /**
     * Test case for successful receipt download.
     */
    @Test
    void downloadReceipt_whenTransactionValid_returnsPdf() throws Exception {

        Long transactionId = 1L;

        Resource resource =
                new ByteArrayResource("pdf-content".getBytes());

        Mockito.when(receiptService.getOrGenerateReceipt(transactionId))
                .thenReturn(resource);

        mockMvc.perform(get(BASE_URL + "/{transactionId}/download", transactionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"receipt-1.pdf\""));

        verify(receiptService).getOrGenerateReceipt(transactionId);
    }

    /**
     * Test case for receipt download with an invalid transaction ID.
     */
    @Test
    void downloadReceipt_whenTransactionIdInvalid_returnsBadRequest() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{transactionId}/download", -1))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(receiptService);
    }
}
