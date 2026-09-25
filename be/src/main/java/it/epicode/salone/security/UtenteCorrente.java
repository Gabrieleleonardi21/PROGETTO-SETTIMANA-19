package it.epicode.salone.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

// L'identita' dell'utente arriva sempre dal subject del token, mai da un campo della richiesta
public final class UtenteCorrente {

    private UtenteCorrente() {
    }

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
