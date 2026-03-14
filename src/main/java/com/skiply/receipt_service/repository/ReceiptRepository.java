package com.skiply.receipt_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skiply.receipt_service.entity.Receipt;

public interface ReceiptRepository extends JpaRepository<Receipt, String> {

    Optional<Receipt> findByTransactionId(Long transactionId);

}
