package it.epicode.salone.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant scadenza, UtenteResponse utente) {
}
