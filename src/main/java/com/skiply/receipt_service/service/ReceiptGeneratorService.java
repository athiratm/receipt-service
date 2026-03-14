package com.skiply.receipt_service.service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.skiply.receipt_service.dto.CreateReceiptRequest;
import com.skiply.receipt_service.dto.ReceiptItemDto;

@Service
public class ReceiptGeneratorService {

    public byte[] generatePdf(CreateReceiptRequest receipt, String referenceNumber) {

        try {

            String html = loadTemplate();

            html = html.replace("{{studentName}}", receipt.studentName());
            html = html.replace("{{transactionDate}}", receipt.paymentDate().toString());
            html = html.replace("{{studentId}}", "SID");
            html = html.replace("{{referenceNumber}}", receipt.referenceNumber());
            html = html.replace("{{cardNumber}}", receipt.cardNumber());
            html = html.replace("{{cardType}}", receipt.cardType());
            html = html.replace("{{totalAmount}}", receipt.totalAmount().toString());

            StringBuilder purchaseItemsHtml = new StringBuilder();
            for (ReceiptItemDto item : receipt.receiptItems()) {

                purchaseItemsHtml.append("""
                    <div class="purchase">
                        <div class="purchase-row">
                            <span><strong>%s</strong></span>
                            <span><strong>%d</strong></span>
                            <span><strong>AED %s</strong></span>

                            <div></div>
                            <div class="item-detail"><small>%d x %s</small></div>
                            <div></div>
                        </div>
                    </div>
                    """.formatted(
                        item.purchaseItem(),
                        item.quantity(),
                        item.unitPrice(),
                        item.quantity(),
                        item.purchaseItem()
                ));
            }

            html = html.replace("{{purchaseItems}}", purchaseItemsHtml.toString());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            saveReceipt(outputStream.toByteArray(), referenceNumber);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating receipt PDF", e);
        }
    }

    private String loadTemplate() throws IOException {

        InputStream inputStream =
                getClass().getResourceAsStream("/templates/skiply_receipt_template.html");

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private void saveReceipt(byte[] pdfBytes, String receiptId) throws IOException {

        String path = "receipts/" + receiptId + ".pdf";

        FileOutputStream fos = new FileOutputStream(path);
        fos.write(pdfBytes);
        fos.close();
    }
}
