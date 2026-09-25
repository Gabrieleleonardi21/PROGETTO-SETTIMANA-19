package it.epicode.salone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Dal profilo si cambia solo il nome: email, ruolo e password restano fuori
public record ProfiloRequest(
        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 60, message = "Massimo 60 caratteri")
        String nome
) {
}
