package it.epicode.salone.dto;

import it.epicode.salone.entities.Alimentazione;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

// Creazione e modifica di un'auto da parte dell'amministratore
public record AutoRequest(
        @NotBlank(message = "La marca è obbligatoria") @Size(max = 60) String marca,
        @NotBlank(message = "Il modello è obbligatorio") @Size(max = 80) String modello,
        @NotNull(message = "L'anno è obbligatorio") @Min(value = 1900, message = "Anno non valido") @Max(value = 2100, message = "Anno non valido") Integer anno,
        @NotNull(message = "I km sono obbligatori") @PositiveOrZero(message = "Non può essere negativo") Integer km,
        @NotNull(message = "L'alimentazione è obbligatoria") Alimentazione alimentazione,
        @Size(max = 2000, message = "Massimo 2000 caratteri") String descrizione,
        @NotNull(message = "Il prezzo è obbligatorio") @Positive(message = "Deve essere positivo") @Digits(integer = 10, fraction = 2) BigDecimal prezzo,
        @NotNull(message = "Il prezzo d'acquisto è obbligatorio") @Positive(message = "Deve essere positivo") @Digits(integer = 10, fraction = 2) BigDecimal prezzoAcquisto,
        boolean pubblicata
) {
}
