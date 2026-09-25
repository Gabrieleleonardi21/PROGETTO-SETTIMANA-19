package it.epicode.salone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "utenti")
@Getter
@Setter
@NoArgsConstructor
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Sempre salvata in minuscolo: due registrazioni con maiuscole diverse sono lo stesso utente
    @Column(nullable = false, unique = true)
    private String email;

    // Hash BCrypt, mai la password in chiaro
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ruolo ruolo = Ruolo.USER;

    @Column(nullable = false, updatable = false)
    private Instant creatoIl = Instant.now();
}
