package com.paymentgateway.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Clase de pruebas unitarias para PaymentResponse
 * Valida la creación correcta de respuestas de pago y sus propiedades
 */
@DisplayName("PaymentResponse Tests")
public class paymentResponseTest {

    // Variables compartidas para todos los tests - datos válidos por defecto
    private boolean success;
    private String gatewayTransactionId;
    private String paymentReference;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String message;
    private String errorCode;
    private LocalDateTime processedAt;
    private GatewaySpecificData gatewayData;

    /**
     * Configuración inicial que se ejecuta ANTES de cada test individual
     * Inicializa variables con datos válidos para una respuesta exitosa
     */
    @BeforeEach
    void setUp() {
        // Datos para una respuesta de pago exitosa
        success = true;
        gatewayTransactionId = "TXN_1234567890";
        paymentReference = "REF_1234567890";
        amount = new BigDecimal("100.00");
        currency = "USD";
        status = PaymentStatus.COMPLETED;
        message = "Payment processed successfully";
        errorCode = null; // Para pagos exitosos no hay código de error
        processedAt = LocalDateTime.now();

        // Datos específicos del gateway (providerId, rawResponse, fees, additionalInfo)
        gatewayData = new GatewaySpecificData(
            "stripe", // providerId
            "{\"charge_id\":\"ch_123\"}", // rawResponse en JSON
            "2.90", // fees - comisión del gateway
            "Stripe payment processed successfully" // additionalInfo
        );
    }

    /**
     * CASO EXITOSO: Valida que se puede crear una respuesta de pago exitosa
     * Verifica que todos los campos se asignen correctamente para un pago completado
     */
    @Test
    @DisplayName("Debería crear una respuesta de pago exitosa")
    void deberiaCrearUnaRespuestaDePagoExitosa() {
        // ARRANGE: Los datos ya están preparados en setUp()

        // ACT: Crear la respuesta de pago con datos válidos
        PaymentResponse response = new PaymentResponse(
            success, // true - pago exitoso
            gatewayTransactionId, // ID de transacción del gateway
            paymentReference, // referencia del pago original
            amount, // monto procesado
            currency, // moneda
            status, // COMPLETED
            message, // mensaje de éxito
            errorCode, // null para pagos exitosos
            processedAt, // timestamp del procesamiento
            gatewayData // datos específicos del gateway
        );

        // ASSERT: Verificar que todos los campos se asignaron correctamente

        // Verificar estado de éxito
        assertThat(response.success()).isTrue(); // Debe ser exitoso

        // Verificar identificadores
        assertThat(response.gatewayTransactionId()).isEqualTo(gatewayTransactionId); // ID de transacción correcto
        assertThat(response.paymentReference()).isEqualTo(paymentReference); // Referencia original correcta

        // Verificar datos monetarios
        assertThat(response.amount()).isEqualByComparingTo(amount); // Monto exacto (BigDecimal)
        assertThat(response.currency()).isEqualTo(currency); // Moneda correcta

        // Verificar estado y mensajes
        assertThat(response.status()).isEqualTo(status); // Estado COMPLETED
        assertThat(response.message()).isEqualTo(message); // Mensaje de éxito
        assertThat(response.errorCode()).isNull(); // No debe haber código de error en pagos exitosos

        // Verificar timestamp - debe ser muy reciente (usando el nombre correcto del campo)
        assertThat(response.processedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        // Verificar datos del gateway usando los nombres correctos del record
        assertThat(response.gatewayData()).isEqualTo(gatewayData); // Datos específicos del gateway
        assertThat(response.gatewayData().providerId()).isEqualTo("stripe"); // Proveedor correcto
        assertThat(response.gatewayData().fees()).isEqualTo("2.90"); // Comisión correcta
        assertThat(response.gatewayData().additionalInfo()).isEqualTo("Stripe payment processed successfully"); // Info adicional
    }

    /**
     * CASO DE ERROR: Valida que se puede crear una respuesta de pago fallida
     * Verifica que los campos de error se manejen correctamente
     */
    @Test
    @DisplayName("Debería crear una respuesta de pago fallida")
    void deberiaCrearUnaRespuestaDePagoFallida() {
        // ARRANGE: Preparar datos para un pago fallido
        boolean failedSuccess = false;
        PaymentStatus failedStatus = PaymentStatus.FAILED;
        String errorMessage = "Insufficient funds";
        String failedErrorCode = "INSUFFICIENT_FUNDS";

        // ACT: Crear respuesta de pago fallido
        PaymentResponse response = new PaymentResponse(
            failedSuccess, // false - pago fallido
            null, // no hay ID de transacción en fallos
            paymentReference, // referencia del pago original
            amount, // monto que se intentó procesar
            currency, // moneda
            failedStatus, // FAILED
            errorMessage, // mensaje de error
            failedErrorCode, // código de error específico
            processedAt, // timestamp del intento
            null // no hay datos del gateway en fallos
        );

        // ASSERT: Verificar que los campos de error se asignaron correctamente
        assertThat(response.success()).isFalse(); // Debe ser fallido
        assertThat(response.gatewayTransactionId()).isNull(); // No hay ID de transacción
        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED); // Estado FAILED
        assertThat(response.message()).isEqualTo(errorMessage); // Mensaje de error
        assertThat(response.errorCode()).isEqualTo(failedErrorCode); // Código de error específico
        assertThat(response.gatewayData()).isNull(); // No hay datos del gateway en fallos

        // Los datos del pago original deben mantenerse
        assertThat(response.paymentReference()).isEqualTo(paymentReference);
        assertThat(response.amount()).isEqualByComparingTo(amount);
        assertThat(response.currency()).isEqualTo(currency);
    }

    /**
     * CASO ESPECÍFICO: Valida el método estático success() para crear respuestas exitosas
     * Verifica que el factory method funcione correctamente
     */
    @Test
    @DisplayName("Debería crear respuesta exitosa usando método factory")
    void deberiaCrearRespuestaExitosaUsandoMetodoFactory() {
        // ACT: Usar el método factory para crear respuesta exitosa
        PaymentResponse response = PaymentResponse.success(
            gatewayTransactionId,
            paymentReference,
            amount,
            currency,
            gatewayData
        );

        // ASSERT: Verificar que se creó correctamente como exitosa
        assertThat(response.success()).isTrue(); // Debe ser exitoso por defecto
        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED); // Estado debe ser COMPLETED
        assertThat(response.gatewayTransactionId()).isEqualTo(gatewayTransactionId);
        assertThat(response.paymentReference()).isEqualTo(paymentReference);
        assertThat(response.amount()).isEqualByComparingTo(amount);
        assertThat(response.currency()).isEqualTo(currency);
        assertThat(response.gatewayData()).isEqualTo(gatewayData);
        assertThat(response.errorCode()).isNull(); // No debe haber error
        assertThat(response.processedAt()).isNotNull(); // Debe tener timestamp
    }

    /**
     * CASO ESPECÍFICO: Valida el método estático failure() para crear respuestas fallidas
     * Verifica que el factory method para fallos funcione correctamente
     */
    @Test
    @DisplayName("Debería crear respuesta fallida usando método factory")
    void deberiaCrearRespuestaFallidaUsandoMetodoFactory() {
        // ARRANGE
        String errorMessage = "Card declined";
        String errorCode = "CARD_DECLINED";

        // ACT: Usar el método factory para crear respuesta fallida
        PaymentResponse response = PaymentResponse.failure(paymentReference, errorMessage, errorCode);

        // ASSERT: Verificar que se creó correctamente como fallida
        assertThat(response.success()).isFalse(); // Debe ser fallido
        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED); // Estado debe ser FAILED
        assertThat(response.paymentReference()).isEqualTo(paymentReference);
        assertThat(response.message()).isEqualTo(errorMessage); // Mensaje de error
        assertThat(response.errorCode()).isEqualTo(errorCode); // Código de error específico
        assertThat(response.gatewayData()).isNull(); // No debe haber datos del gateway en fallos
        assertThat(response.processedAt()).isNotNull(); // Debe tener timestamp del intento
    }
}
