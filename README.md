# Receipt Service

## Overview
The **Receipt Service** is a Spring Boot microservice responsible for generating, storing, and retrieving payment receipts. It integrates with the Fee Service to fetch transaction details and generates PDF receipts for students and parents.

## Features
- **Generate Receipts**: Creates PDF receipts for successful fee payments.
- **Download Receipts**: Provides an API to download existing receipts.
- **Microservice Integration**: Communicates with `fee-service` using Feign Client.
- **Resilience**: Implements Circuit Breaker and Retry mechanisms using Resilience4j.
- **Database**: Uses H2 in-memory database for storing receipt metadata.

## Tech Stack
- **Java 21**
- **Spring Boot 3.5.x**
- **Spring Cloud OpenFeign**
- **Resilience4j** (Circuit Breaker, Retry)
- **H2 Database**
- **OpenHTMLtoPDF** (PDF Generation)
- **Lombok**
- **JUnit 5 & Mockito**

## API Endpoints

### 1. Create Receipt
**POST** `/api/v1/receipts`

Create a new receipt manually (mostly used internally or for testing).

**Request Body:**
```json
{
  "transactionId": 12345,
  "studentName": "John Doe",
  "schoolName": "Springfield High",
  "totalAmount": 150.00,
  "paymentDate": "2024-02-25T10:00:00",
  "referenceNumber": "TXN-98765",
  "cardNumber": "************1234",
  "cardType": "VISA",
  "receiptItems": [
    {
      "purchaseItem": "Tuition Fee",
      "quantity": 1,
      "unitPrice": 150.00
    }
  ]
}
```

### 2. Download Receipt
**GET** `/api/v1/receipts/{transactionId}/download`

Downloads the PDF receipt for a given transaction ID. If the receipt does not exist, it fetches details from `fee-service` and generates one.

## Configuration
The service is configured in `application.yml`. Key configurations include:

- **Server Port**: `8082`
- **Eureka Server**: `http://localhost:9090/eureka/`
- **H2 Console**: `http://localhost:8082/h2-console`
- **Swagger UI**: `http://localhost:8082/swagger-ui.html`

## Running the Service

### Prerequisites
- JDK 21
- Maven

### Build and Run
```sh
mvn clean install
mvn spring-boot:run
```

## Resilience & Fault Tolerance
The service uses **Resilience4j** to handle failures when communicating with external services (`fee-service`):
- **Retry**: Retries failed requests up to 3 times.
- **Circuit Breaker**: Opens the circuit if 50% of requests fail, preventing cascading failures.


