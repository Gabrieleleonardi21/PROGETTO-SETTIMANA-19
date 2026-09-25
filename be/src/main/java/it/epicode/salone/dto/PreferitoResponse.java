package it.epicode.salone.dto;

import it.epicode.salone.entities.Preferito;

import java.util.UUID;

public record PreferitoResponse(UUID id, AutoPubblicaResponse auto) {

    public static PreferitoResponse da(Preferito p) {
        return new PreferitoResponse(p.getId(), AutoPubblicaResponse.da(p.getAuto()));
    }
}
