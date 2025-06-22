package com.paymentgateway.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.paymentgateway.shared.exception.PaymentException;

/**
 * Clase de pruebas unitarias para la entidad Payment
 * Valida la creación correcta de pagos y las transiciones de estado
 */
@DisplayName("Payment Entity Tests")
class PaymentTest {

    // Variables compartidas para todos los tests - datos válidos por defecto
    private String paymentReference;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String gatewayProvider;
    private String customerId;
    private String merchantId;
    private String description;

    /**
     * Configuración inicial que se ejecuta ANTES de cada test individual
     * Inicializa variables con datos válidos para evitar repetir código
     */
    @BeforeEach
    void setUp() {
        paymentReference = "test-ref-123";
        amount = new BigDecimal("100.50");
        currency = "USD";
        paymentMethod = PaymentMethod.CREDIT_CARD;
        gatewayProvider = "TEST_PROVIDER";
        customerId = "cust-123";
        merchantId = "merch-456";
        description = "Test Payment";
    }

    /**
     * Grupo de tests para validar la creación correcta de pagos
     * Incluye casos exitosos y validaciones de entrada
     */
    @Nested
    @DisplayName("Payment Creation")
    class PaymentCreationTests {

        /**
         * CASO EXITOSO: Valida que se puede crear un pago con datos válidos
         * Verifica que todos los campos se asignen correctamente y el estado inicial sea PENDING
         */
        @Test
        @DisplayName("Should create a payment successfully with valid data")
        void shouldCreatePaymentSuccessfully() {
            // ARRANGE: Los datos ya están preparados en setUp()
            
            // ACT: Crear el pago con datos válidos
            Payment payment = new Payment(paymentReference, amount, currency, paymentMethod, gatewayProvider, customerId, merchantId, description);

            // ASSERT: Verificar que todos los campos se asignaron correctamente
            assertThat(payment.getId()).isNotNull(); // El ID debe generarse automáticamente
            assertThat(payment.getPaymentReference()).isEqualTo(paymentReference); // Referencia debe coincidir
            assertThat(payment.getAmount()).isEqualByComparingTo(amount); // Monto debe ser exacto (BigDecimal)
            assertThat(payment.getCurrency()).isEqualTo(currency); // Moneda debe coincidir
            assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod); // Método de pago correcto
            assertThat(payment.getGatewayProvider()).isEqualTo(gatewayProvider); // Proveedor correcto
            assertThat(payment.getCustomerId()).isEqualTo(customerId); // Cliente correcto
            assertThat(payment.getMerchantId()).isEqualTo(merchantId); // Comercio correcto
            assertThat(payment.getDescription()).isEqualTo(description); // Descripción correcta
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING); // Estado inicial debe ser PENDING
            
            // Verificar timestamps - la fecha de creación debe ser muy reciente
            assertThat(payment.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            
            // Campos que deben estar vacíos en un pago recién creado
            assertThat(payment.getUpdatedAt()).isNull(); // Aún no se ha actualizado
            assertThat(payment.getCompletedAt()).isNull(); // Aún no se ha completado
            assertThat(payment.getFailureReason()).isNull(); // No hay razón de fallo
            assertThat(payment.getGatewayTransactionId()).isNull(); // Aún no hay ID de transacción
        }

        /**
         * VALIDACIÓN: El constructor debe rechazar montos nulos
         * Valida que la regla de negocio "el monto es obligatorio" se cumple
         */
        @Test
        @DisplayName("Should throw PaymentException for null amount")
        void shouldThrowExceptionForNullAmount() {
            // ACT & ASSERT: Intentar crear pago con amount = null debe fallar
            assertThatThrownBy(() -> new Payment(paymentReference, null, currency, paymentMethod, gatewayProvider, customerId, merchantId, description))
                .isInstanceOf(PaymentException.class) // Debe lanzar la excepción específica del dominio
                .hasMessage("Payment amount must be positive"); // Con el mensaje exacto esperado
        }

        /**
         * VALIDACIÓN: El constructor debe rechazar montos en cero
         * Valida que la regla de negocio "el monto debe ser positivo" se cumple
         */
        @Test
        @DisplayName("Should throw PaymentException for zero amount")
        void shouldThrowExceptionForZeroAmount() {
            // ACT & ASSERT: Monto = 0 debe ser rechazado
            assertThatThrownBy(() -> new Payment(paymentReference, BigDecimal.ZERO, currency, paymentMethod, gatewayProvider, customerId, merchantId, description))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Payment amount must be positive");
        }

        /**
         * VALIDACIÓN: El constructor debe rechazar montos negativos
         * Valida que no se pueden crear pagos con montos negativos
         */
        @Test
        @DisplayName("Should throw PaymentException for negative amount")
        void shouldThrowExceptionForNegativeAmount() {
            // ACT & ASSERT: Monto negativo debe ser rechazado
            assertThatThrownBy(() -> new Payment(paymentReference, new BigDecimal("-10.00"), currency, paymentMethod, gatewayProvider, customerId, merchantId, description))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Payment amount must be positive");
        }

        /**
         * VALIDACIÓN: El constructor debe rechazar monedas vacías o con espacios
         * Valida que la moneda es un campo obligatorio
         */
        @Test
        @DisplayName("Should throw PaymentException for empty currency")
        void shouldThrowExceptionForEmptyCurrency() {
            // ACT & ASSERT: Moneda vacía (solo espacios) debe ser rechazada
            assertThatThrownBy(() -> new Payment(paymentReference, amount, "  ", paymentMethod, gatewayProvider, customerId, merchantId, description))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Currency is required");
        }

        /**
         * VALIDACIÓN: El constructor debe rechazar métodos de pago nulos
         * Valida que el método de pago es obligatorio
         */
        @Test
        @DisplayName("Should throw PaymentException for null payment method")
        void shouldThrowExceptionForNullPaymentMethod() {
            // ACT & ASSERT: PaymentMethod = null debe ser rechazado
            assertThatThrownBy(() -> new Payment(paymentReference, amount, currency, null, gatewayProvider, customerId, merchantId, description))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Payment method is required");
        }
    }

    /**
     * Grupo de tests para validar las transiciones de estado del pago
     * Verifica que los cambios de estado sigan las reglas de negocio correctas
     */
    @Nested
    @DisplayName("State Transitions")
    class StateTransitionTests {

        private Payment payment;

        /**
         * Configuración específica para tests de transición de estado
         * Crea un pago válido antes de cada test de este grupo
         */
        @BeforeEach
        void createPayment() {
            payment = new Payment(paymentReference, amount, currency, paymentMethod, gatewayProvider, customerId, merchantId, description);
        }

        /**
         * TRANSICIÓN VÁLIDA: PENDING → PROCESSING
         * Valida que un pago en estado PENDING puede cambiar a PROCESSING
         */
        @Test
        @DisplayName("markAsProcessing should succeed when status is PENDING")
        void markAsProcessing_whenPending_shouldSucceed() {
            // ARRANGE: El pago ya está en estado PENDING (creado en @BeforeEach)
            
            // ACT: Marcar como procesando
            payment.markAsProcessing();

            // ASSERT: Verificar que el estado cambió correctamente
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING); // Estado debe ser PROCESSING
            assertThat(payment.getUpdatedAt()).isNotNull(); // Se debe registrar cuándo se actualizó
        }

        /**
         * TRANSICIÓN INVÁLIDA: Solo se puede cambiar a PROCESSING desde PENDING
         * Valida que la regla de negocio de transiciones de estado se cumple
         */
        @Test
        @DisplayName("markAsProcessing should throw exception when status is not PENDING")
        void markAsProcessing_whenNotPending_shouldThrowException() {
            // ARRANGE: Cambiar el estado para que NO esté en PENDING
            payment.markAsProcessing(); // Ahora está en PROCESSING
            payment.markAsCompleted("txn_123"); // Ahora está en COMPLETED

            // ACT & ASSERT: Intentar cambiar a PROCESSING desde COMPLETED debe fallar
            assertThatThrownBy(() -> payment.markAsProcessing())
                .isInstanceOf(PaymentException.class)
                .hasMessage("Payment can only be marked as processing from pending status");
        }

        /**
         * TRANSICIÓN VÁLIDA: PROCESSING → COMPLETED
         * Valida que un pago en PROCESSING puede completarse correctamente
         */
        @Test
        @DisplayName("markAsCompleted should succeed when status is PROCESSING")
        void markAsCompleted_whenProcessing_shouldSucceed() {
            // ARRANGE: Poner el pago en estado PROCESSING
            payment.markAsProcessing();
            String gatewayTxnId = "txn_completed_123";

            // ACT: Completar el pago
            payment.markAsCompleted(gatewayTxnId);

            // ASSERT: Verificar que se completó correctamente
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED); // Estado debe ser COMPLETED
            assertThat(payment.getGatewayTransactionId()).isEqualTo(gatewayTxnId); // Se debe guardar el ID de transacción
            assertThat(payment.getCompletedAt()).isNotNull(); // Se debe registrar cuándo se completó
            assertThat(payment.getUpdatedAt()).isNotNull(); // Se debe actualizar la fecha de modificación
        }

        /**
         * TRANSICIÓN INVÁLIDA: Solo se puede completar desde PROCESSING
         * Valida que no se puede completar un pago que no esté siendo procesado
         */
        @Test
        @DisplayName("markAsCompleted should throw exception when status is not PROCESSING")
        void markAsCompleted_whenNotProcessing_shouldThrowException() {
            // ARRANGE: El pago está en PENDING (no en PROCESSING)
            
            // ACT & ASSERT: Intentar completar desde PENDING debe fallar
            assertThatThrownBy(() -> payment.markAsCompleted("txn_123"))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Payment can only be completed from processing status");
        }

        /**
         * TRANSICIÓN VÁLIDA: Cualquier estado (excepto COMPLETED) → FAILED
         * Valida que se puede marcar como fallido si no está completado
         */
        @Test
        @DisplayName("markAsFailed should succeed when status is not COMPLETED")
        void markAsFailed_whenNotCompleted_shouldSucceed() {
            // ARRANGE: El pago está en PENDING (no completado)
            String reason = "Insufficient funds";

            // ACT: Marcar como fallido
            payment.markAsFailed(reason);

            // ASSERT: Verificar que se marcó como fallido correctamente
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED); // Estado debe ser FAILED
            assertThat(payment.getFailureReason()).isEqualTo(reason); // Se debe guardar la razón del fallo
            assertThat(payment.getUpdatedAt()).isNotNull(); // Se debe actualizar la fecha
        }

        /**
         * TRANSICIÓN INVÁLIDA: No se puede fallar un pago completado
         * Valida la regla de negocio: "un pago completado no puede fallar"
         */
        @Test
        @DisplayName("markAsFailed should throw exception when status is COMPLETED")
        void markAsFailed_whenCompleted_shouldThrowException() {
            // ARRANGE: Completar el pago primero
            payment.markAsProcessing(); // PENDING → PROCESSING
            payment.markAsCompleted("txn_123"); // PROCESSING → COMPLETED

            // ACT & ASSERT: Intentar fallar un pago completado debe lanzar excepción
            assertThatThrownBy(() -> payment.markAsFailed("Late failure"))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Cannot fail a completed payment");
        }

        /**
         * TRANSICIÓN INVÁLIDA: No se puede cancelar un pago completado
         * Valida la regla de negocio: "un pago completado no puede cancelarse"
         */
        @Test
        @DisplayName("cancel should throw exception when status is COMPLETED")
        void cancel_whenCompleted_shouldThrowException() {
            // ARRANGE: Completar el pago primero
            payment.markAsProcessing(); // PENDING → PROCESSING
            payment.markAsCompleted("txn_123"); // PROCESSING → COMPLETED

            // ACT & ASSERT: Intentar cancelar un pago completado debe fallar
            assertThatThrownBy(() -> payment.cancel())
                .isInstanceOf(PaymentException.class)
                .hasMessage("Cannot cancel a completed payment");
        }
    }
}