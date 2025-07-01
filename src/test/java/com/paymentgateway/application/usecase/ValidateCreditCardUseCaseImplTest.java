package com.paymentgateway.application.usecase;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.paymentgateway.domain.model.CreditCardDetails;
import com.paymentgateway.domain.model.CreditCardValidationResult;

/**
 * @helper ValidateCreditCardUseCaseImplTest
 * @description Tests unitarios para validación de tarjetas de crédito
 */
@DisplayName("ValidateCreditCardUseCase Tests")
class ValidateCreditCardUseCaseImplTest {

    private ValidateCreditCardUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ValidateCreditCardUseCaseImpl();
    }

    @Nested
    @DisplayName("Validación de Tarjetas VISA")
    class ValidacionTarjetasVisa {

        @Test
        @DisplayName("✅ Debe validar tarjeta VISA correctamente")
        void debeValidarTarjetaVisaCorrectamente() {
            // Arrange
            CreditCardDetails tarjetaVisa = crearTarjetaVisa();
            
            // Act
            CreditCardValidationResult resultado = useCase.validateCreditCard(tarjetaVisa);
            
            // Assert
            verificarTarjetaValida(resultado, "VISA");
        }

        @Test
        @DisplayName("✅ Debe detectar tipo VISA correctamente")
        void debeDetectarTipoVisaCorrectamente() {
            // Act & Assert
            assertThat(useCase.determineCardType("4242424242424242")).isEqualTo("VISA");
        }
    }

    @Nested
    @DisplayName("Validación de Tarjetas MasterCard")
    class ValidacionTarjetasMasterCard {

        @Test
        @DisplayName("✅ Debe validar tarjeta MasterCard correctamente")
        void debeValidarTarjetaMasterCardCorrectamente() {
            // Arrange
            CreditCardDetails tarjetaMaster = crearTarjetaMasterCard();
            
            // Act
            CreditCardValidationResult resultado = useCase.validateCreditCard(tarjetaMaster);
            
            // Assert
            verificarTarjetaValida(resultado, "MASTERCARD");
        }

        @Test
        @DisplayName("✅ Debe detectar tipo MasterCard correctamente")
        void debeDetectarMasterCardCorrectamente() {
            // Act & Assert
            assertThat(useCase.determineCardType("5555555555554444")).isEqualTo("MASTERCARD");
        }
    }

    @Nested
    @DisplayName("Validación de Tarjetas Inválidas")
    class ValidacionTarjetasInvalidas {

        @Test
        @DisplayName("❌ Debe rechazar número inválido por Luhn")
        void debeRechazarNumeroInvalidoPorLuhn() {
            // Arrange
            CreditCardDetails tarjetaInvalida = crearTarjetaInvalida();
            
            // Act
            CreditCardValidationResult resultado = useCase.validateCreditCard(tarjetaInvalida);
            
            // Assert
            verificarTarjetaInvalida(resultado);
        }

        @Test
        @DisplayName("❌ Debe retornar UNKNOWN para número desconocido")
        void debeRetornarUnknownParaNumeroDesconocido() {
            // Act & Assert
            assertThat(useCase.determineCardType("1234567890123456")).isEqualTo("UNKNOWN");
        }
    }

    @Nested
    @DisplayName("Validación de Expiración")
    class ValidacionExpiracion {

        @Test
        @DisplayName("❌ Debe rechazar tarjeta expirada en constructor")
        void debeRechazarTarjetaExpiradaEnConstructor() {
            // Act & Assert
            assertThatThrownBy(() -> 
                new CreditCardDetails("4242424242424242", "01", "2020", "123", "Expired User")
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("La tarjeta ha expirado");
        }

        @Test
        @DisplayName("✅ Debe crear tarjeta con fecha futura válida")
        void debeCrearTarjetaConFechaFuturaValida() {
            // Arrange & Act
            CreditCardDetails tarjetaFutura = new CreditCardDetails(
                "4242424242424242", "12", "2030", "123", "Future User"
            );
            CreditCardValidationResult resultado = useCase.validateCreditCard(tarjetaFutura);
            
            // Assert
            assertThat(resultado.isValid()).isTrue();
            assertThat(resultado.isExpired()).isFalse();
            assertThat(resultado.daysUntilExpiry()).isGreaterThan(1000); // Más de 1000 días para 2030
        }
    }

    // Métodos auxiliares (factories)
    private CreditCardDetails crearTarjetaVisa() {
        return new CreditCardDetails("4242424242424242", "12", "2028", "123", "Juan Perez");
    }

    private CreditCardDetails crearTarjetaMasterCard() {
        return new CreditCardDetails("5555555555554444", "12", "2028", "123", "Maria Garcia");
    }

    private CreditCardDetails crearTarjetaInvalida() {
        return new CreditCardDetails("1234567890123456", "12", "2028", "123", "Test User");
    }



    private void verificarTarjetaValida(CreditCardValidationResult resultado, String tipoEsperado) {
        assertThat(resultado.isValid()).isTrue();
        assertThat(resultado.cardType()).isEqualTo(tipoEsperado);
        assertThat(resultado.maskedCardNumber()).contains("****");
        assertThat(resultado.isExpired()).isFalse();
    }

    private void verificarTarjetaInvalida(CreditCardValidationResult resultado) {
        assertThat(resultado.isValid()).isFalse();
        assertThat(resultado.message()).contains("Numero de tarjeta invalido");
    }
}
