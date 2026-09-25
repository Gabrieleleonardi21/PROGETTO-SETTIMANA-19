package it.epicode.salone.dto;

import it.epicode.salone.entities.Alimentazione;
import it.epicode.salone.entities.Auto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Vista dell'amministratore: anche prezzo d'acquisto e bozze
public record AutoAdminResponse(
        UUID id,
        String marca,
        String modello,
        int anno,
        int km,
        Alimentazione alimentazione,
        String descrizione,
        BigDecimal prezzo,
        BigDecimal prezzoAcquisto,
        boolean pubblicata,
        Instant creataIl,
        long versione,
        List<String> immagini,
        String creditiFoto,
        String fonteFoto
) {
    public static AutoAdminResponse da(Auto a) {
        return new AutoAdminResponse(a.getId(), a.getMarca(), a.getModello(), a.getAnno(), a.getKm(),
                a.getAlimentazione(), a.getDescrizione(), a.getPrezzo(), a.getPrezzoAcquisto(),
                a.isPubblicata(), a.getCreataIl(), a.getVersione(), List.copyOf(a.getImmagini()),
                a.getCreditiFoto(), a.getFonteFoto());
    }
}
