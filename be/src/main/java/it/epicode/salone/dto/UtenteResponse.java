package it.epicode.salone.dto;

import it.epicode.salone.entities.Ruolo;
import it.epicode.salone.entities.Utente;

import java.util.UUID;

public record UtenteResponse(UUID id, String nome, String email, Ruolo ruolo) {

    public static UtenteResponse da(Utente u) {
        return new UtenteResponse(u.getId(), u.getNome(), u.getEmail(), u.getRuolo());
    }
}
