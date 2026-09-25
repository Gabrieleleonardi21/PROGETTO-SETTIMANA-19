package it.epicode.salone.controllers;

import it.epicode.salone.dto.PreferitoRequest;
import it.epicode.salone.dto.PreferitoResponse;
import it.epicode.salone.security.UtenteCorrente;
import it.epicode.salone.services.PreferitoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/preferiti")
@RequiredArgsConstructor
public class PreferitoController {

    private final PreferitoService preferitoService;

    @GetMapping
    public List<PreferitoResponse> miei(@AuthenticationPrincipal Jwt jwt) {
        return preferitoService.miei(UtenteCorrente.id(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreferitoResponse aggiungi(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid PreferitoRequest req) {
        return preferitoService.aggiungi(UtenteCorrente.id(jwt), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rimuovi(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        preferitoService.rimuovi(UtenteCorrente.id(jwt), id);
    }
}
