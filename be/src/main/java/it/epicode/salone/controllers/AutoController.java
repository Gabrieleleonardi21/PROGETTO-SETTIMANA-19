package it.epicode.salone.controllers;

import it.epicode.salone.dto.AutoPubblicaResponse;
import it.epicode.salone.dto.AutoSearchParams;
import it.epicode.salone.dto.PageResponse;
import it.epicode.salone.services.AutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Catalogo pubblico: solo auto pubblicate e senza prezzo d'acquisto
@RestController
@RequestMapping("/api/auto")
@RequiredArgsConstructor
public class AutoController {

    private final AutoService autoService;

    // es. /api/auto?q=panda&prezzoMax=15000&sort=prezzo,asc&page=0&size=12
    @GetMapping
    public PageResponse<AutoPubblicaResponse> catalogo(AutoSearchParams filtri,
                                                      @PageableDefault(size = 12) Pageable pageable) {
        return autoService.catalogo(filtri, pageable);
    }

    @GetMapping("/{id}")
    public AutoPubblicaResponse dettaglio(@PathVariable UUID id) {
        return autoService.dettaglioPubblico(id);
    }
}
