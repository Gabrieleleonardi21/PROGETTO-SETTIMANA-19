package it.epicode.salone.dto;

import it.epicode.salone.entities.Avviso;

import java.math.BigDecimal;
import java.util.UUID;

public record AvvisoResponse(UUID id, UUID autoId, String marca, String modello, BigDecimal prezzoAttuale,
                             BigDecimal soglia, boolean inviato) {

    public static AvvisoResponse da(Avviso a) {
        return new AvvisoResponse(a.getId(), a.getAuto().getId(), a.getAuto().getMarca(), a.getAuto().getModello(),
                a.getAuto().getPrezzo(), a.getSoglia(), a.isInviato());
    }
}
