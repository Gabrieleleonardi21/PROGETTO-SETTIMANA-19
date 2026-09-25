package it.epicode.salone.events;

import it.epicode.salone.repositories.AvvisoRepository;
import it.epicode.salone.services.AvvisoService;
import it.epicode.salone.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

/**
 * Ascolta i ribassi di prezzo.
 * AFTER_COMMIT: parte solo se il salvataggio e' andato a buon fine; se fallisce non parte nulla.
 * @Async: gira in un altro thread, l'amministratore riceve la risposta senza aspettare Gmail.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvvisoListener {

    private final AvvisoRepository avvisoRepository;
    private final AvvisoService avvisoService;
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPrezzoCambiato(PrezzoCambiatoEvent evento) {
        List<UUID> candidati = candidati(evento);
        for (UUID avvisoId : candidati) {
            try {
                // Se un altro thread ha gia' preso questo avviso, prendiInCarico torna vuoto e non si spedisce
                avvisoService.prendiInCarico(avvisoId, evento.nuovo())
                        .ifPresent(mail -> emailService.inviaAvvisoPrezzo(avvisoId, mail));
            } catch (RuntimeException e) {
                // Un avviso andato storto (es. DB momentaneamente giu') non deve bloccare gli altri
                log.error("Avviso {} non elaborato", avvisoId, e);
            }
        }
        // Nei log solo id e numeri, mai indirizzi email
        log.info("Ribasso auto {}: {} avvisi candidati", evento.autoId(), candidati.size());
    }

    private List<UUID> candidati(PrezzoCambiatoEvent evento) {
        if (evento.vecchio() == null) {
            return avvisoRepository.daInviareAllaPubblicazione(evento.autoId(), evento.nuovo());
        }
        return avvisoRepository.daInviare(evento.autoId(), evento.vecchio(), evento.nuovo());
    }
}
