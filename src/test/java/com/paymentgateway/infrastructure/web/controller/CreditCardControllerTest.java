package com.paymentgateway.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.application.port.in.ValidateCreditCardUseCase;
import com.paymentgateway.domain.model.CreditCardDetails;
import com.paymentgateway.domain.model.CreditCardValidationResult;

/**
 * @helper CreditCardControllerTest
 * @description Tests de integración para el controlador de tarjetas de crédito
 */
@WebMvcTest(CreditCardController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CreditCardController Integration Tests")
class CreditCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ValidateCreditCardUseCase validateCreditCardUseCase;

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
        void debeValidarTarjetaExitosamente() throws Exception {
            // Arrange
            when(validateCreditCardUseCase.validateCreditCard(any(CreditCardDetails.class)))
                .thenReturn(resultadoValido);

            // Act & Assert
            mockMvc.perform(post("/api/v1/credit-cards/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tarjetaValida)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(true))
                    .andExpect(jsonPath("$.cardType").value("VISA"))
                    .andExpect(jsonPath("$.maskedCardNumber").value("****-****-****-4242"));
        }

        @Test
        @DisplayName("❌ Debe manejar tarjeta inválida")
        void debeManejarTarjetaInvalida() throws Exception {
            // Arrange
            when(validateCreditCardUseCase.validateCreditCard(any(CreditCardDetails.class)))
                .thenReturn(resultadoInvalido);

            // Act & Assert
            mockMvc.perform(post("/api/v1/credit-cards/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tarjetaValida)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(false));
        }
    }

    @Nested
    @DisplayName("Endpoint GET /card-type")
    class EndpointCardType {

        @Test
        @DisplayName("✅ Debe retornar tipo VISA")
        void debeRetornarTipoVisa() throws Exception {
            // Arrange
            when(validateCreditCardUseCase.determineCardType(anyString())).thenReturn("VISA");

            // Act & Assert
            mockMvc.perform(get("/api/v1/credit-cards/card-type/4242424242424242"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("VISA"));
        }

        @Test
        @DisplayName("✅ Debe retornar tipo UNKNOWN")
        void debeRetornarTipoUnknown() throws Exception {
            // Arrange
            when(validateCreditCardUseCase.determineCardType(anyString())).thenReturn("UNKNOWN");

            // Act & Assert
            mockMvc.perform(get("/api/v1/credit-cards/card-type/1234567890123456"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("Endpoint GET /health")
    class EndpointHealth {

        @Test
        @DisplayName("✅ Debe retornar estado de salud")
        void debeRetornarEstadoDeSalud() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/credit-cards/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Credit Card validation service is up and running!"));
        }
    }

    private void configurarDatosPrueba() {
        tarjetaValida = new CreditCardDetails("4242424242424242", "12", "2028", "123", "Juan Perez");
        
        resultadoValido = CreditCardValidationResult.valid("VISA", "****-****-****-4242", 365);
        
        resultadoInvalido = CreditCardValidationResult.invalid("Número de tarjeta inválido");
    }
}