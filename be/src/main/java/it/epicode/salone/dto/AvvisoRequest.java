package it.epicode.salone.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

// Niente utenteId ne' inviato: il primo arriva dal token, il secondo lo gestisce solo il server
public record AvvisoRequest(
        @NotNull(message = "Auto obbligatoria") UUID autoId,
        @NotNull(message = "La soglia è obbligatoria")
        @Positive(message = "Deve essere positiva")
        @Digits(integer = 10, fraction = 2)
        BigDecimal soglia
) {
}
