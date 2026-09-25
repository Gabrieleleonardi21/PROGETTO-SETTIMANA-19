package it.epicode.salone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Solo questi tre campi: un "ruolo" aggiunto al JSON viene ignorato da Jackson
public record RegistrazioneRequest(
        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 60, message = "Massimo 60 caratteri")
        String nome,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Email non valida")
        @Size(max = 120, message = "Massimo 120 caratteri")
        String email,

        // Il limite a 72 e' quello di BCrypt: oltre, i caratteri verrebbero ignorati
        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 8, max = 72, message = "Da 8 a 72 caratteri")
        String password
) {
}
