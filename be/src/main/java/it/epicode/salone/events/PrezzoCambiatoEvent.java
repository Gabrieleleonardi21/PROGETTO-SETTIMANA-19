package it.epicode.salone.events;

import java.math.BigDecimal;
import java.util.UUID;

// Pubblicato da AutoService quando un'auto pubblicata scende di prezzo.
// vecchio == null: l'auto e' appena passata da bozza a pubblicata (vedi AutoService.modifica).
public record PrezzoCambiatoEvent(UUID autoId, BigDecimal vecchio, BigDecimal nuovo) {
}
