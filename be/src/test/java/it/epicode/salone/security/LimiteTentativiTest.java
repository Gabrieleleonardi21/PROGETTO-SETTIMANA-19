package it.epicode.salone.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimiteTentativiTest {

    // Orologio che il test fa avanzare a mano, per non aspettare un minuto vero
    private final AtomicLong adesso = new AtomicLong(0);
    private final Clock clock = new Clock() {
        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(adesso.get());
        }
    };

    @Test
    void bloccaOltreIlMassimoERiapreAlMinutoSuccessivo() {
        LimiteTentativi limite = new LimiteTentativi(3, clock);

        assertTrue(limite.consenti("1.2.3.4"));
        assertTrue(limite.consenti("1.2.3.4"));
        assertTrue(limite.consenti("1.2.3.4"));
        assertFalse(limite.consenti("1.2.3.4"), "il quarto tentativo nello stesso minuto va rifiutato");

        adesso.addAndGet(60_000);
        assertTrue(limite.consenti("1.2.3.4"), "nella finestra successiva si riparte da zero");
    }

    @Test
    void ogniIpHaIlSuoConteggio() {
        LimiteTentativi limite = new LimiteTentativi(1, clock);

        assertTrue(limite.consenti("1.1.1.1"));
        assertFalse(limite.consenti("1.1.1.1"));
        assertTrue(limite.consenti("2.2.2.2"), "un IP bloccato non deve bloccare gli altri");
    }
}
