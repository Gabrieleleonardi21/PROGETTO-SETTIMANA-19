package it.epicode.salone.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PrezzoRequest(
        @NotNull(message = "Il prezzo è obbligatorio")
        @Positive(message = "Deve essere positivo")
        @Digits(integer = 10, fraction = 2)
        BigDecimal prezzo,
        // Versione dell'auto vista dall'admin: se nel frattempo un altro l'ha cambiata si risponde 409
        @NotNull(message = "Versione mancante: ricarica la pagina")
        Long versione
) {
}
