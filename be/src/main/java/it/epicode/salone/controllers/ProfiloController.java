package it.epicode.salone.controllers;

import it.epicode.salone.dto.ProfiloRequest;
import it.epicode.salone.dto.UtenteResponse;
import it.epicode.salone.security.UtenteCorrente;
import it.epicode.salone.services.UtenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

// Profilo dell'utente collegato: l'id arriva sempre dal token
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfiloController {

    private final UtenteService utenteService;

    @GetMapping
    public UtenteResponse profilo(@AuthenticationPrincipal Jwt jwt) {
        return utenteService.profilo(UtenteCorrente.id(jwt));
    }

    @PutMapping
    public UtenteResponse aggiorna(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid ProfiloRequest req) {
        return utenteService.aggiornaProfilo(UtenteCorrente.id(jwt), req);
    }

    // "Elimina il mio account": cancella anche avvisi e preferiti
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@AuthenticationPrincipal Jwt jwt) {
        utenteService.elimina(UtenteCorrente.id(jwt));
    }
}
