package it.epicode.salone.services;

import it.epicode.salone.dto.AvvisoRequest;
import it.epicode.salone.dto.AvvisoResponse;
import it.epicode.salone.entities.Auto;
import it.epicode.salone.entities.Avviso;
import it.epicode.salone.events.MailAvviso;
import it.epicode.salone.repositories.AvvisoRepository;
import it.epicode.salone.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvvisoService {

    private final AvvisoRepository avvisoRepository;
    private final UtenteService utenteService;
    private final AutoService autoService;

    @Transactional(readOnly = true)
    public List<AvvisoResponse> miei(UUID utenteId) {
        return avvisoRepository.findByUtenteIdOrderByCreatoIlDesc(utenteId).stream()
                .map(AvvisoResponse::da)
                .toList();
    }

    // Un avviso per auto. La soglia deve stare sotto il prezzo attuale, altrimenti non potrebbe mai "attraversarla".
    @Transactional
    public AvvisoResponse crea(UUID utenteId, AvvisoRequest req) {
        if (avvisoRepository.existsByUtenteIdAndAutoId(utenteId, req.autoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hai già un avviso per questa auto");
        }
        Auto auto = autoService.trovaPubblicata(req.autoId());
        if (req.soglia().compareTo(auto.getPrezzo()) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La soglia deve essere sotto il prezzo attuale");
        }
        Avviso avviso = new Avviso(utenteService.trova(utenteId), auto, req.soglia());
        return AvvisoResponse.da(avvisoRepository.save(avviso));
    }

    // Id + proprietario insieme: l'avviso di un altro utente e' 404
    @Transactional
    public void elimina(UUID utenteId, UUID id) {
        Avviso avviso = avvisoRepository.findByIdAndUtenteId(id, utenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avviso non trovato"));
        avvisoRepository.delete(avviso);
    }

    // Link della mail: il token e' monouso perche' l'avviso viene eliminato, e con lui l'hash
    @Transactional
    public void disattivaConToken(String token) {
        Avviso avviso = avvisoRepository.findByTokenDisattivaHash(TokenHasher.sha256(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link non valido o già usato"));
        avvisoRepository.delete(avviso);
    }

    /**
     * Prende il segno "inviato" con l'UPDATE atomico e, solo se la riga aggiornata e' una, prepara i dati della mail.
     * Transazione propria: il listener gira dopo il commit del cambio prezzo, fuori da quella transazione.
     * Il segno resta preso anche se poi Gmail fallisce: meglio una mail persa che una doppia.
     */
    @Transactional
    public Optional<MailAvviso> prendiInCarico(UUID avvisoId, BigDecimal nuovoPrezzo) {
        String token = TokenHasher.nuovoToken();
        if (avvisoRepository.prendiInCarico(avvisoId, TokenHasher.sha256(token)) != 1) {
            return Optional.empty();
        }
        Avviso a = avvisoRepository.findById(avvisoId).orElseThrow();
        return Optional.of(new MailAvviso(a.getUtente().getEmail(), a.getUtente().getNome(),
                a.getAuto().getMarca(), a.getAuto().getModello(), nuovoPrezzo, a.getSoglia(), token));
    }
}
