package it.epicode.salone.controllers;

import it.epicode.salone.dto.*;
import it.epicode.salone.services.AutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Solo amministratore: un utente collegato che ci prova riceve 403
@RestController
@RequestMapping("/api/admin/auto")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAutoController {

    private final AutoService autoService;

    @GetMapping
    public PageResponse<AutoAdminResponse> tutte(@PageableDefault(size = 20) Pageable pageable) {
        return autoService.tutte(pageable);
    }

    @GetMapping("/{id}")
    public AutoAdminResponse dettaglio(@PathVariable UUID id) {
        return autoService.dettaglioAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutoAdminResponse crea(@RequestBody @Valid AutoRequest req) {
        return autoService.crea(req);
    }

    @PutMapping("/{id}")
    public AutoAdminResponse modifica(@PathVariable UUID id, @RequestBody @Valid AutoRequest req) {
        return autoService.modifica(id, req);
    }

    @PatchMapping("/{id}/prezzo")
    public AutoAdminResponse cambiaPrezzo(@PathVariable UUID id, @RequestBody @Valid PrezzoRequest req) {
        return autoService.cambiaPrezzo(id, req);
    }
}
