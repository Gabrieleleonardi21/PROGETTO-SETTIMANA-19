package it.epicode.salone.services;

import it.epicode.salone.dto.*;
import it.epicode.salone.entities.Utente;
import it.epicode.salone.repositories.AvvisoRepository;
import it.epicode.salone.repositories.PreferitoRepository;
import it.epicode.salone.repositories.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final PreferitoRepository preferitoRepository;
    private final AvvisoRepository avvisoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Hash BCrypt di una password casuale, calcolato una volta sola all'avvio
    private String hashFittizio;

    @PostConstruct
    void preparaHashFittizio() {
        hashFittizio = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    // Il ruolo non arriva dal client: new Utente() parte sempre da USER
    @Transactional
    public LoginResponse registra(RegistrazioneRequest req) {
        // BCrypt accetta al massimo 72 byte: con lettere accentate 72 caratteri possono superarli
        if (req.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password troppo lunga");
        }
        String email = normalizza(req.email());
        if (utenteRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email già registrata");
        }
        Utente utente = new Utente();
        utente.setNome(req.nome().trim());
        utente.setEmail(email);
        utente.setPassword(passwordEncoder.encode(req.password()));
        utenteRepository.save(utente);
        return jwtService.emetti(utente);
    }

    /**
     * Stesso messaggio e stesso tempo per email sconosciuta e password sbagliata: non si rivela quali email esistono.
     * Con un'email sconosciuta BCrypt gira comunque su un hash fittizio, cosi' la risposta non e' piu' veloce.
     */
    public LoginResponse login(LoginRequest req) {
        Optional<Utente> trovato = utenteRepository.findByEmail(normalizza(req.email()));
        String hash = trovato.map(Utente::getPassword).orElse(hashFittizio);
        boolean ok = passwordEncoder.matches(req.password(), hash);
        if (trovato.isEmpty() || !ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
        }
        return jwtService.emetti(trovato.get());
    }

    public UtenteResponse profilo(UUID utenteId) {
        return UtenteResponse.da(trova(utenteId));
    }

    @Transactional
    public UtenteResponse aggiornaProfilo(UUID utenteId, ProfiloRequest req) {
        Utente utente = trova(utenteId);
        utente.setNome(req.nome().trim());
        return UtenteResponse.da(utente);
    }

    // "Elimina il mio account": prima avvisi e preferiti, poi l'utente. Da qui non parte piu' nessuna mail.
    @Transactional
    public void elimina(UUID utenteId) {
        Utente utente = trova(utenteId);
        avvisoRepository.eliminaDiUtente(utenteId);
        preferitoRepository.eliminaDiUtente(utenteId);
        utenteRepository.delete(utente);
    }

    // Usato anche da preferiti e avvisi: un token di un account eliminato non vale piu' niente
    Utente trova(UUID utenteId) {
        return utenteRepository.findById(utenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account non trovato"));
    }

    private static String normalizza(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
