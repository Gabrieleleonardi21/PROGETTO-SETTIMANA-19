package it.epicode.salone.controllers;

import it.epicode.salone.dto.AvvisoRequest;
import it.epicode.salone.dto.AvvisoResponse;
import it.epicode.salone.dto.DisattivaRequest;
import it.epicode.salone.security.UtenteCorrente;
import it.epicode.salone.services.AvvisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/avvisi")
@RequiredArgsConstructor
public class AvvisoController {

    private final AvvisoService avvisoService;

    @GetMapping
    public List<AvvisoResponse> miei(@AuthenticationPrincipal Jwt jwt) {
        return avvisoService.miei(UtenteCorrente.id(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvvisoResponse crea(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid AvvisoRequest req) {
        return avvisoService.crea(UtenteCorrente.id(jwt), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        avvisoService.elimina(UtenteCorrente.id(jwt), id);
    }

    // Pubblico: chi apre il link della mail non deve per forza essere collegato.
    // POST e non GET: i client di posta aprono i link in anteprima e consumerebbero il token.
    @PostMapping("/disattiva")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disattiva(@RequestBody @Valid DisattivaRequest req) {
        avvisoService.disattivaConToken(req.token());
    }
}
