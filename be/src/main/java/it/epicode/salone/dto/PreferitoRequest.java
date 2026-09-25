package it.epicode.salone.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// L'utente arriva dal token, mai dal body
public record PreferitoRequest(@NotNull(message = "Auto obbligatoria") UUID autoId) {
}
