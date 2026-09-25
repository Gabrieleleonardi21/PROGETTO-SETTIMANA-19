package it.epicode.salone.dto;

import it.epicode.salone.entities.Alimentazione;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

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
        // Boolean + @NotNull: un JSON senza il campo non deve mettere l'auto in bozza di nascosto
        @NotNull(message = "Indica se l'auto è pubblicata") Boolean pubblicata,
        // Versione letta dall'admin quando ha aperto il form: in modifica e' obbligatoria (null in creazione)
        Long versione,
        // Foto facoltative: al massimo 8, solo https e solo dagli host ammessi (controllo in ImmaginiAuto)
        @Size(max = 8, message = "Massimo 8 foto") List<@NotBlank @Size(max = 500) String> immagini,
        @Size(max = 200, message = "Massimo 200 caratteri") String creditiFoto,
        @Size(max = 500) String fonteFoto
) {
}
