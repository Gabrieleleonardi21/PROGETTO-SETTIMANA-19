package it.epicode.salone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "auto")
@Getter
@Setter
@NoArgsConstructor
public class Auto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modello;

    @Column(nullable = false)
    private int anno;

    @Column(nullable = false)
    private int km;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Alimentazione alimentazione;

    // Testo semplice: il FE lo mostra sempre come testo, mai come HTML
    @Column(length = 2000)
    private String descrizione;

    // Prezzo di vendita, pubblico
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prezzo;

    // Prezzo pagato dal salone: lo vede solo l'amministratore
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prezzoAcquisto;

    // false = bozza, visibile solo all'amministratore
    @Column(nullable = false)
    private boolean pubblicata;

    @Column(nullable = false, updatable = false)
    private Instant creataIl = Instant.now();

    // Due modifiche contemporanee alla stessa auto: la seconda riceve 409 invece di sovrascrivere
    @Version
    private long versione;

    // URL delle foto, nell'ordine della galleria. Solo host ammessi (vedi ImmaginiAuto).
    // BatchSize: nel catalogo le foto di tutte le auto della pagina arrivano con poche query invece di una per auto
    @ElementCollection
    @CollectionTable(name = "auto_immagini", joinColumns = @JoinColumn(name = "auto_id"))
    @OrderColumn(name = "posizione")
    @Column(name = "url", nullable = false, length = 500)
    @BatchSize(size = 50)
    private List<String> immagini = new ArrayList<>();

    // Crediti delle foto (licenza) e pagina da cui provengono, mostrati sotto la galleria
    @Column(length = 200)
    private String creditiFoto;

    @Column(length = 500)
    private String fonteFoto;
}
