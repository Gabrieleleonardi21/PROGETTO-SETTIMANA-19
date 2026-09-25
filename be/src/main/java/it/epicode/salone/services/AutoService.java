package it.epicode.salone.services;

import it.epicode.salone.dto.*;
import it.epicode.salone.entities.Auto;
import it.epicode.salone.events.PrezzoCambiatoEvent;
import it.epicode.salone.repositories.AutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutoService {

    // Elenco chiuso dei campi ordinabili: nome pubblico -> proprieta' JPA. Tutto il resto e' 400.
    private static final Map<String, String> ORDINAMENTI = Map.of(
            "prezzo", "prezzo",
            "anno", "anno",
            "km", "km",
            "marca", "marca",
            "recenti", "creataIl");
    private static final Sort ORDINE_PREDEFINITO = Sort.by(Sort.Direction.DESC, "creataIl");
    // Nel catalogo pubblico prima le auto con la copertina, poi le altre: dentro ogni gruppo vale l'ordine scelto.
    // Non e' tra gli ORDINAMENTI ammessi: il client non lo puo' togliere ne' usare da solo.
    private static final Sort PRIMA_CON_COPERTINA = Sort.by(Sort.Direction.DESC, "conCopertina");

    private final AutoRepository autoRepository;
    private final ApplicationEventPublisher eventi;

    // ---------- Pubblico ----------

    @Transactional(readOnly = true)
    public PageResponse<AutoPubblicaResponse> catalogo(AutoSearchParams filtri, Pageable pageable) {
        Sort sort = PRIMA_CON_COPERTINA.and(SearchUtils.traduciSort(pageable.getSort(), ORDINAMENTI, ORDINE_PREDEFINITO));
        Pageable pagina = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return PageResponse.of(autoRepository.findAll(AutoSpecifications.pubbliche(filtri), pagina)
                .map(AutoPubblicaResponse::da));
    }

    @Transactional(readOnly = true)
    public AutoPubblicaResponse dettaglioPubblico(UUID id) {
        return AutoPubblicaResponse.da(trovaPubblicata(id));
    }

    // Usato da preferiti e avvisi: si possono seguire solo auto pubblicate
    Auto trovaPubblicata(UUID id) {
        return autoRepository.findByIdAndPubblicataTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto non trovata"));
    }

    // ---------- Amministratore ----------

    @Transactional(readOnly = true)
    public PageResponse<AutoAdminResponse> tutte(Pageable pageable) {
        Sort sort = SearchUtils.traduciSort(pageable.getSort(), ORDINAMENTI, ORDINE_PREDEFINITO);
        Pageable pagina = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return PageResponse.of(autoRepository.findAll(pagina).map(AutoAdminResponse::da));
    }

    @Transactional(readOnly = true)
    public AutoAdminResponse dettaglioAdmin(UUID id) {
        return AutoAdminResponse.da(trova(id));
    }

    @Transactional
    public AutoAdminResponse crea(AutoRequest req) {
        Auto auto = new Auto();
        copia(req, auto);
        auto.setPrezzo(req.prezzo());
        return AutoAdminResponse.da(autoRepository.saveAndFlush(auto));
    }

    // La modifica passa dallo stesso punto del cambio prezzo, cosi' anche qui scattano gli avvisi
    @Transactional
    public AutoAdminResponse modifica(UUID id, AutoRequest req) {
        Auto auto = trova(id);
        controllaVersione(auto, req.versione());
        boolean eraBozza = !auto.isPubblicata();
        copia(req, auto);
        if (eraBozza && auto.isPubblicata()) {
            // Bozza -> pubblicata: mentre era in bozza il prezzo puo' essere sceso senza avvisare nessuno.
            // Evento senza prezzo vecchio: AvvisoListener cerca gli avvisi con soglia gia' raggiunta.
            auto.setPrezzo(req.prezzo());
            eventi.publishEvent(new PrezzoCambiatoEvent(auto.getId(), null, req.prezzo()));
        } else {
            aggiornaPrezzo(auto, req.prezzo());
        }
        // flush: la versione incrementata finisce subito nella risposta, pronta per la modifica successiva
        return AutoAdminResponse.da(autoRepository.saveAndFlush(auto));
    }

    @Transactional
    public AutoAdminResponse cambiaPrezzo(UUID id, PrezzoRequest req) {
        Auto auto = trova(id);
        controllaVersione(auto, req.versione());
        aggiornaPrezzo(auto, req.prezzo());
        return AutoAdminResponse.da(autoRepository.saveAndFlush(auto));
    }

    /**
     * @Version da solo protegge due salvataggi nello stesso istante, non un form rimasto aperto:
     * l'admin A apre il form, B abbassa il prezzo, A salva il valore vecchio e annulla B senza accorgersene.
     * Confrontando la versione vista da A con quella attuale, A riceve 409 e ricarica.
     */
    private static void controllaVersione(Auto auto, Long versioneVista) {
        if (versioneVista == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Versione mancante: ricarica la pagina");
        }
        if (versioneVista != auto.getVersione()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "L'auto è stata modificata da un altro amministratore: ricarica la pagina.");
        }
    }

    /**
     * Unico punto in cui cambia il prezzo. Se l'auto e' pubblicata e il prezzo scende pubblica
     * PrezzoCambiatoEvent: la mail la manda AvvisoListener solo DOPO il commit di questa transazione.
     */
    private void aggiornaPrezzo(Auto auto, BigDecimal nuovo) {
        BigDecimal vecchio = auto.getPrezzo();
        auto.setPrezzo(nuovo);
        if (auto.isPubblicata() && nuovo.compareTo(vecchio) < 0) {
            eventi.publishEvent(new PrezzoCambiatoEvent(auto.getId(), vecchio, nuovo));
        }
    }

    // Tutti i campi tranne il prezzo, che passa sempre da aggiornaPrezzo
    private static void copia(AutoRequest req, Auto auto) {
        auto.setMarca(req.marca().trim());
        auto.setModello(req.modello().trim());
        auto.setAnno(req.anno());
        auto.setKm(req.km());
        auto.setAlimentazione(req.alimentazione());
        auto.setDescrizione(req.descrizione());
        auto.setPrezzoAcquisto(req.prezzoAcquisto());
        auto.setPubblicata(req.pubblicata());
        // Le foto si sostituiscono in blocco: la lista arriva gia' nell'ordine della galleria
        auto.getImmagini().clear();
        auto.getImmagini().addAll(ImmaginiAuto.foto(req.immagini()));
        auto.setCreditiFoto(testoOpzionale(req.creditiFoto()));
        auto.setFonteFoto(ImmaginiAuto.fonte(req.fonteFoto()));
    }

    private static String testoOpzionale(String testo) {
        if (testo == null || testo.isBlank()) {
            return null;
        }
        return testo.trim();
    }

    private Auto trova(UUID id) {
        return autoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto non trovata"));
    }
}
