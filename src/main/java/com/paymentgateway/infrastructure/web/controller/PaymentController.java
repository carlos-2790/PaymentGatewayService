package com.paymentgateway.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.application.port.in.ProcessPaymentUseCase;
import com.paymentgateway.domain.model.Payment;
import com.paymentgateway.domain.model.PaymentRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "API para procesamiento de pagos")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;

    @PostMapping
    @Operation(
        summary = "Procesar un nuevo pago",
        description = "Procesa un pago usando la pasarela configurada y retorna el resultado del procesamiento"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Pago procesado exitosamente",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Payment.class),
                    examples = @ExampleObject(
                        name = "Pago exitoso",
                        summary = "Ejemplo de respuesta exitosa",
                        value = """
                        {
                          "id": "0be0f74c-7710-4a91-a49b-8b225d61d968",
                          "paymentReference": "test-ref-123",
                          "amount": 100.50,
                          "currency": "USD",
                          "status": "COMPLETED",
                          "paymentMethod": "CREDIT_CARD",
                          "gatewayProvider": "stripe",
                          "gatewayTransactionId": "txn_123456789",
                          "customerId": "cust-123",
                          "merchantId": "merch-456",
                          "description": "Test Payment",
                          "createdAt": "2025-06-25T19:32:48.1317594",
                          "updatedAt": "2025-06-25T19:32:49.1317594",
                          "completedAt": "2025-06-25T19:32:49.1317594",
                          "failureReason": null,
                          "version": 2,
                          "completed": true,
                          "failed": false,
                          "pending": false
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Error de validación",
                        value = """
                        {
                          "timestamp": "2025-06-25T22:30:19.555+00:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Payment amount must be positive",
                          "path": "/api/v1/payments"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Error interno del servidor",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Error interno",
                        value = """
                        {
                          "timestamp": "2025-06-25T22:30:19.555+00:00",
                          "status": 500,
                          "error": "Internal Server Error",
                          "message": "Payment processing failed",
                          "path": "/api/v1/payments"
                        }
                        """
                    )
                )
            )
        }
    )
    public ResponseEntity<Payment> processPayment(
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del pago a procesar",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentRequestDTO.class),
                examples = {
                    @ExampleObject(
                        name = "Pago con tarjeta de crédito",
                        summary = "Ejemplo de pago con tarjeta de crédito",
                        value = """
                        {
                          "paymentReference": "test-ref-123",
                          "amount": 100.50,
                          "currency": "USD",
                          "paymentMethod": "CREDIT_CARD",
                          "customerId": "cust-123",
                          "merchantId": "merch-456",
                          "description": "Test Payment",
                          "paymentDetails": {
                            "type": "CREDIT_CARD",
                            "cardNumber": "4242424242424242",
                            "expiryMonth": "12",
                            "expiryYear": "2028",
                            "cvv": "123",
                            "cardHolderName": "Juan Perez"
                          }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Pago con PayPal",
                        summary = "Ejemplo de pago con PayPal",
                        value = """
                        {
                          "paymentReference": "paypal-ref-456",
                          "amount": 250.75,
                          "currency": "EUR",
                          "paymentMethod": "PAYPAL",
                          "customerId": "cust-456",
                          "merchantId": "merch-789",
                          "description": "PayPal Payment",
                          "paymentDetails": {
                            "type": "PAYPAL",
                            "email": "user@example.com",
                            "returnUrl": "https://merchant.com/success",
                            "cancelUrl": "https://merchant.com/cancel"
                          }
                        }
                        """
                    )
                }
            )
        ) PaymentRequestDTO paymentRequestDTO
    ) {
        Payment payment = processPaymentUseCase.processPayment(paymentRequestDTO.toPaymentRequest());
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verificar estado del servicio",
        description = "Endpoint para verificar que el servicio de pagos está funcionando correctamente"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Servicio funcionando correctamente",
        content = @Content(
            mediaType = "text/plain",
            examples = @ExampleObject(name = "Health check exitoso", value = "Payment service is up and running!")
        )
    )
    public String healthCheck() {
        return "Payment service is up and running!";
    }
}
