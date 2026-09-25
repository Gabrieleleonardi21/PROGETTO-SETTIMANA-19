package it.epicode.salone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// Un'auto compare al massimo una volta tra i preferiti di un utente
@Entity
@Table(name = "preferiti", uniqueConstraints = @UniqueConstraint(columnNames = {"utente_id", "auto_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Preferito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auto_id")
    private Auto auto;

    @Column(nullable = false, updatable = false)
    private Instant creatoIl = Instant.now();

    public Preferito(Utente utente, Auto auto) {
        this.utente = utente;
        this.auto = auto;
    }
}
