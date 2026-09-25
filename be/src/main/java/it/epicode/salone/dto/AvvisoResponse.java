package it.epicode.salone.dto;

import it.epicode.salone.entities.Auto;
import it.epicode.salone.entities.Avviso;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * disponibile = false: l'auto e' tornata in bozza. Marca, modello e prezzo restano null
 * perche' i dati di una bozza non devono uscire; l'utente puo' comunque eliminare l'avviso.
 */
public record AvvisoResponse(UUID id, UUID autoId, boolean disponibile, String marca, String modello,
                             BigDecimal prezzoAttuale, BigDecimal soglia, boolean inviato) {

    public static AvvisoResponse da(Avviso a) {
        Auto auto = a.getAuto();
        if (!auto.isPubblicata()) {
            return new AvvisoResponse(a.getId(), auto.getId(), false, null, null, null, a.getSoglia(), a.isInviato());
        }
        return new AvvisoResponse(a.getId(), auto.getId(), true, auto.getMarca(), auto.getModello(),
                auto.getPrezzo(), a.getSoglia(), a.isInviato());
    }
}
