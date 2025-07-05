package com.paymentgateway.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.paymentgateway.application.port.in.ValidateCreditCardUseCase;
import com.paymentgateway.domain.model.CreditCardDetails;
import com.paymentgateway.domain.model.CreditCardValidationResult;
import com.paymentgateway.infrastructure.web.dto.CreditCardValidationRequest;

/**
 * @helper CreditCardControllerTest
 * @description Tests unitarios para el controlador de tarjetas de crédito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreditCardController Unit Tests")
class CreditCardControllerTest {

    @Mock
    private ValidateCreditCardUseCase validateCreditCardUseCase;

    @InjectMocks
    private CreditCardController creditCardController;

    private CreditCardDetails tarjetaValida;
    private CreditCardValidationResult resultadoValido;
    private CreditCardValidationResult resultadoInvalido;

    @BeforeEach
    void setUp() {
        configurarDatosPrueba();
    }

    @Nested
    @DisplayName("Endpoint POST /validate")
    class EndpointValidate {

        @Test
        @DisplayName("✅ Debe validar tarjeta exitosamente")
        void debeValidarTarjetaExitosamente() {
            // Arrange
            CreditCardValidationRequest request = new CreditCardValidationRequest(
                "4242424242424242", "12", "2028", "123", "Juan Perez"
            );
            when(validateCreditCardUseCase.validateCreditCard(any(CreditCardDetails.class)))
                .thenReturn(resultadoValido);

            // Act
            ResponseEntity<CreditCardValidationResult> response = 
                creditCardController.validateCreditCard(request);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isValid());
            assertEquals("VISA", response.getBody().cardType());
            assertEquals("****-****-****-4242", response.getBody().maskedCardNumber());
        }

        @Test
        @DisplayName("❌ Debe manejar tarjeta inválida")
        void debeManejarTarjetaInvalida() {
            // Arrange
            CreditCardValidationRequest request = new CreditCardValidationRequest(
                "1234567890123456", "12", "2028", "123", "Juan Perez"
            );
            when(validateCreditCardUseCase.validateCreditCard(any(CreditCardDetails.class)))
                .thenReturn(resultadoInvalido);

            // Act
            ResponseEntity<CreditCardValidationResult> response = 
                creditCardController.validateCreditCard(request);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isValid());
        }
    }

    @Nested
    @DisplayName("Endpoint GET /card-type")
    class EndpointCardType {

        @Test
        @DisplayName("✅ Debe retornar tipo VISA")
        void debeRetornarTipoVisa() {
            // Arrange
            when(validateCreditCardUseCase.determineCardType("4242424242424242")).thenReturn("VISA");

            // Act
            ResponseEntity<String> response = creditCardController.getCardType("4242424242424242");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("VISA", response.getBody());
        }

        @Test
        @DisplayName("✅ Debe retornar tipo UNKNOWN")
        void debeRetornarTipoUnknown() {
            // Arrange
            when(validateCreditCardUseCase.determineCardType("1234567890123456")).thenReturn("UNKNOWN");

            // Act
            ResponseEntity<String> response = creditCardController.getCardType("1234567890123456");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("UNKNOWN", response.getBody());
        }
    }

    @Nested
    @DisplayName("Endpoint GET /health")
    class EndpointHealth {

        @Test
        @DisplayName("✅ Debe retornar estado de salud")
        void debeRetornarEstadoDeSalud() {
            // Act
            ResponseEntity<String> response = creditCardController.healthCheck();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Credit Card validation service is up and running!", response.getBody());
        }
    }

    private void configurarDatosPrueba() {
        tarjetaValida = new CreditCardDetails("4242424242424242", "12", "2028", "123", "Juan Perez");
        
        resultadoValido = CreditCardValidationResult.valid("VISA", "****-****-****-4242", 365);
        
        resultadoInvalido = CreditCardValidationResult.invalid("Número de tarjeta inválido");
    }
}