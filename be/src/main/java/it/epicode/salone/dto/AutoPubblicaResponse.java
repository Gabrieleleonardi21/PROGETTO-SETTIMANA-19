package it.epicode.salone.dto;

import it.epicode.salone.entities.Alimentazione;
import it.epicode.salone.entities.Auto;

import java.math.BigDecimal;
import java.util.UUID;

// Quello che vede il pubblico: niente prezzo d'acquisto, niente stato di bozza
public record AutoPubblicaResponse(
        UUID id,
        String marca,
        String modello,
        int anno,
        int km,
        Alimentazione alimentazione,
        String descrizione,
        BigDecimal prezzo
) {
    public static AutoPubblicaResponse da(Auto a) {
        return new AutoPubblicaResponse(a.getId(), a.getMarca(), a.getModello(), a.getAnno(), a.getKm(),
                a.getAlimentazione(), a.getDescrizione(), a.getPrezzo());
    }
}
