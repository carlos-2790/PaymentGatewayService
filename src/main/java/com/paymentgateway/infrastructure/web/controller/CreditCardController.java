package com.paymentgateway.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.application.port.in.ValidateCreditCardUseCase;
import com.paymentgateway.domain.model.CreditCardDetails;
import com.paymentgateway.domain.model.CreditCardValidationResult;
import com.paymentgateway.infrastructure.web.dto.CreditCardValidationRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controlador para operaciones de tarjetas de crédito
 */
@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
@Tag(name = "Credit Cards", description = "API para validación de tarjetas de crédito")
public class CreditCardController {

    private final ValidateCreditCardUseCase validateCreditCardUseCase;

    @PostMapping("/validate")
    @Operation(
        summary = "Validar tarjeta de crédito",
        description = "Valida los detalles de una tarjeta de crédito incluyendo número, fecha de expiración y CVV"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validación completada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreditCardValidationResult.class),
                examples = @ExampleObject(
                    name = "Tarjeta válida",
                    value = """
                    {
                      "isValid": true,
                      "cardType": "VISA",
                      "maskedCardNumber": "****-****-****-4242",
                      "message": "Tarjeta válida",
                      "isExpired": false,
                      "daysUntilExpiry": 365
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de tarjeta inválidos",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Tarjeta inválida",
                    value = """
                    {
                      "isValid": false,
                      "cardType": "UNKNOWN",
                      "maskedCardNumber": "****-****-****-****",
                      "message": "Número de tarjeta inválido",
                      "isExpired": false,
                      "daysUntilExpiry": 0
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<CreditCardValidationResult> validateCreditCard(
        @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Detalles de la tarjeta de crédito a validar",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreditCardValidationRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Tarjeta VISA válida",
                        value = """
                        {
                          "cardNumber": "4242424242424242",
                          "expiryMonth": "12",
                          "expiryYear": "2028",
                          "cvv": "123",
                          "cardHolderName": "Juan Perez"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Tarjeta MASTERCARD válida",
                        value = """
                        {
                          "cardNumber": "5555555555554444",
                          "expiryMonth": "08",
                          "expiryYear": "2027",
                          "cvv": "456",
                          "cardHolderName": "Maria Garcia"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Tarjeta AMEX válida",
                        value = """
                        {
                          "cardNumber": "378282246310005",
                          "expiryMonth": "06",
                          "expiryYear": "2026",
                          "cvv": "1234",
                          "cardHolderName": "Carlos Rodriguez"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Tarjeta desconocida (UNKNOWN)",
                        value = """
                        {
                          "cardNumber": "2345555565654242",
                          "expiryMonth": "12",
                          "expiryYear": "2025",
                          "cvv": "789",
                          "cardHolderName": "Ana Lopez"
                        }
                        """
                    )
                }
            )
        ) CreditCardValidationRequest request
    ) {
        try {
            // Convertir el DTO a CreditCardDetails
            CreditCardDetails creditCardDetails = new CreditCardDetails(
                request.cardNumber(),
                request.expiryMonth(),
                request.expiryYear(),
                request.cvv(),
                request.cardHolderName()
            );
            
            CreditCardValidationResult result = validateCreditCardUseCase.validateCreditCard(creditCardDetails);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // Si hay error en la validación del modelo, devolver resultado inválido
            CreditCardValidationResult errorResult = CreditCardValidationResult.invalid(e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    @GetMapping("/card-type/{cardNumber}")
    @Operation(
        summary = "Determinar tipo de tarjeta",
        description = "Determina el tipo de tarjeta basado en el número proporcionado. " +
                     "Soporta VISA (4xxx), MASTERCARD (5[1-5]xx), AMEX (3[47]xx), DISCOVER (6xxx). " +
                     "Retorna 'UNKNOWN' si no coincide con ningún patrón conocido."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tipo de tarjeta determinado exitosamente",
            content = @Content(
                mediaType = "text/plain",
                examples = {
                    @ExampleObject(
                        name = "VISA válida",
                        value = "VISA",
                        description = "Ejemplo: 4242-4242-4242-4242"
                    ),
                    @ExampleObject(
                        name = "MASTERCARD válida",
                        value = "MASTERCARD",
                        description = "Ejemplo: 5555-5555-5555-4444"
                    ),
                    @ExampleObject(
                        name = "AMEX válida",
                        value = "AMEX",
                        description = "Ejemplo: 3782-822463-10005"
                    ),
                    @ExampleObject(
                        name = "DISCOVER válida",
                        value = "DISCOVER",
                        description = "Ejemplo: 6011-1111-1111-1117"
                    ),
                    @ExampleObject(
                        name = "Tarjeta desconocida",
                        value = "UNKNOWN",
                        description = "Ejemplo: 2345-5555-6565-4242 (no coincide con patrones conocidos)"
                    )
                }
            )
        )
    })
    public ResponseEntity<String> getCardType(
        @PathVariable @io.swagger.v3.oas.annotations.Parameter(
            description = "Número de tarjeta (con o sin guiones/espacios)",
            examples = {
                @ExampleObject(name = "VISA", value = "4242-4242-4242-4242"),
                @ExampleObject(name = "MASTERCARD", value = "5555-5555-5555-4444"),
                @ExampleObject(name = "AMEX", value = "3782-822463-10005"),
                @ExampleObject(name = "DISCOVER", value = "6011-1111-1111-1117"),
                @ExampleObject(name = "Desconocida", value = "2345-5555-6565-4242")
            }
        ) String cardNumber
    ) {
        String cardType = validateCreditCardUseCase.determineCardType(cardNumber);
        return ResponseEntity.ok(cardType);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Verificar estado del servicio de tarjetas",
        description = "Endpoint para verificar que el servicio de validación de tarjetas está funcionando"
    )
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Credit Card validation service is up and running!");
    }
}


