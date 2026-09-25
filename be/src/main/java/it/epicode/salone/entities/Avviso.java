package it.epicode.salone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Lega un utente a un'auto con una soglia di prezzo.
 * inviato passa a true una sola volta (UPDATE atomico in AvvisoRepository.prendiInCarico)
 * e non torna piu' indietro: ogni avviso manda al massimo una mail.
 */
@Entity
@Table(name = "avvisi", uniqueConstraints = @UniqueConstraint(columnNames = {"utente_id", "auto_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Avviso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auto_id")
    private Auto auto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal soglia;

    @Column(nullable = false)
    private boolean inviato;

    // SHA-256 del token del link "disattiva" contenuto nella mail; il token in chiaro non si salva
    @Column(unique = true, length = 64)
    private String tokenDisattivaHash;

    @Column(nullable = false, updatable = false)
    private Instant creatoIl = Instant.now();

    public Avviso(Utente utente, Auto auto, BigDecimal soglia) {
        this.utente = utente;
        this.auto = auto;
        this.soglia = soglia;
    }
}
