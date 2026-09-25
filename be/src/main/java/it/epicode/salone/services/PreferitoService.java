package it.epicode.salone.services;

import it.epicode.salone.dto.PreferitoRequest;
import it.epicode.salone.dto.PreferitoResponse;
import it.epicode.salone.entities.Preferito;
import it.epicode.salone.repositories.PreferitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferitoService {

    private final PreferitoRepository preferitoRepository;
    private final UtenteService utenteService;
    private final AutoService autoService;

    @Transactional(readOnly = true)
    public List<PreferitoResponse> miei(UUID utenteId) {
        return preferitoRepository.findByUtenteIdAndAutoPubblicataTrueOrderByCreatoIlDesc(utenteId).stream()
                .map(PreferitoResponse::da)
                .toList();
    }

    @Transactional
    public PreferitoResponse aggiungi(UUID utenteId, PreferitoRequest req) {
        if (preferitoRepository.existsByUtenteIdAndAutoId(utenteId, req.autoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Auto già tra i preferiti");
        }
        Preferito p = new Preferito(utenteService.trova(utenteId), autoService.trovaPubblicata(req.autoId()));
        return PreferitoResponse.da(preferitoRepository.save(p));
    }

    // Id + proprietario insieme: il preferito di un altro utente e' 404, come se non esistesse
    @Transactional
    public void rimuovi(UUID utenteId, UUID id) {
        Preferito p = preferitoRepository.findByIdAndUtenteId(id, utenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preferito non trovato"));
        preferitoRepository.delete(p);
    }
}
