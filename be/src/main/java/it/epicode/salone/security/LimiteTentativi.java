package it.epicode.salone.security;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Conta i tentativi per chiave (l'IP del client) in finestre fisse di un minuto.
 * Oltre il massimo la richiesta va rifiutata fino alla finestra successiva.
 * In memoria: basta per un'istanza sola, come il piano free di Render.
 */
public class LimiteTentativi {

    private static final long FINESTRA_MS = 60_000;
    // Oltre questa soglia si fa pulizia delle finestre scadute, cosi' la mappa non cresce all'infinito
    private static final int PULIZIA_OLTRE = 10_000;

    private record Finestra(long inizio, int tentativi) {
    }

    private final ConcurrentHashMap<String, Finestra> finestre = new ConcurrentHashMap<>();
    private final int massimo;
    private final Clock clock;

    public LimiteTentativi(int massimo, Clock clock) {
        this.massimo = massimo;
        this.clock = clock;
    }

    /** Registra un tentativo e restituisce true se e' ancora entro il limite. */
    public boolean consenti(String chiave) {
        long adesso = clock.millis();
        if (finestre.size() > PULIZIA_OLTRE) {
            finestre.values().removeIf(f -> adesso - f.inizio() >= FINESTRA_MS);
        }
        // compute e' atomico per chiave: due richieste contemporanee dello stesso IP non si perdono il conteggio
        Finestra f = finestre.compute(chiave, (k, vecchia) -> {
            if (vecchia == null || adesso - vecchia.inizio() >= FINESTRA_MS) {
                return new Finestra(adesso, 1);
            }
            return new Finestra(vecchia.inizio(), vecchia.tentativi() + 1);
        });
        return f.tentativi() <= massimo;
    }

    /** Secondi che mancano alla prossima finestra, per l'header Retry-After. */
    public long secondiAttesa(String chiave) {
        Finestra f = finestre.get(chiave);
        if (f == null) {
            return 0;
        }
        long restanti = FINESTRA_MS - (clock.millis() - f.inizio());
        return Math.max(1, restanti / 1000);
    }
}
