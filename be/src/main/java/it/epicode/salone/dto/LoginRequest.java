package it.epicode.salone.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "L'email è obbligatoria") String email,
        @NotBlank(message = "La password è obbligatoria") String password
) {
}
