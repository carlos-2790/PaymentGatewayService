package com.paymentgateway.application.port.in;

import com.paymentgateway.domain.model.CreditCardDetails;
import com.paymentgateway.domain.model.CreditCardValidationResult;


public interface ValidateCreditCardUseCase {
   /**
    * Valida los detalles de una tarjeta de credito
    * @param creditCardDetails detalles de la tarjeta de credito
    * @return Resultado de la validacion de la tarjeta de credito
    */
   CreditCardValidationResult validateCreditCard(CreditCardDetails creditCardDetails);

   /**
    * Determina el tipo de atrjeta basado en el numero
    * @param cardNumber numero de la tarjeta
    * @return Tipo de tarjeta(VISA, MASTERCARD, etc)
    */
    String determineCardType(String cardNumber);
}
