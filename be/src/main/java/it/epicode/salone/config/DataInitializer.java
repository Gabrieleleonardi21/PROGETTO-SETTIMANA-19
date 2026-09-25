package it.epicode.salone.config;

import it.epicode.salone.entities.Ruolo;
import it.epicode.salone.entities.Utente;
import it.epicode.salone.repositories.UtenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

// Crea l'amministratore al primo avvio. L'unico modo di avere il ruolo ADMIN: la registrazione da' sempre USER.
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        // L'email dell'admin e' prevedibile: almeno la password deve reggere un attacco a dizionario
        if (adminPassword.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD deve avere almeno 12 caratteri");
        }
        String email = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (utenteRepository.existsByEmail(email)) {
            return;
        }
        Utente admin = new Utente();
        admin.setEmail(email);
        admin.setNome("Amministratore");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRuolo(Ruolo.ADMIN);
        utenteRepository.save(admin);
        // Ne' email ne' password nei log
        log.info("Amministratore creato");
    }
}
