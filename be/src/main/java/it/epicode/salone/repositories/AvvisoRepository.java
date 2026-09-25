package it.epicode.salone.repositories;

import it.epicode.salone.entities.Avviso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvvisoRepository extends JpaRepository<Avviso, UUID> {

    @EntityGraph(attributePaths = "auto")
    List<Avviso> findByUtenteIdOrderByCreatoIlDesc(UUID utenteId);

    // Cerca per id E proprietario: chi cambia /api/avvisi/12 in /13 riceve 404
    Optional<Avviso> findByIdAndUtenteId(UUID id, UUID utenteId);

    boolean existsByUtenteIdAndAutoId(UUID utenteId, UUID autoId);

    Optional<Avviso> findByTokenDisattivaHash(String tokenDisattivaHash);

    /**
     * Avvisi che il cambio di prezzo ha attraversato: prima il prezzo era sopra la soglia
     * (soglia < vecchio), adesso e' uguale o sotto (soglia >= nuovo).
     * Stesso prezzo salvato di nuovo o ulteriore ribasso sotto la soglia: non entrano.
     */
    @Query("""
            SELECT a.id FROM Avviso a
            WHERE a.auto.id = :autoId
              AND a.inviato = false
              AND a.soglia < :vecchio
              AND a.soglia >= :nuovo
            """)
    List<UUID> daInviare(UUID autoId, BigDecimal vecchio, BigDecimal nuovo);

    /**
     * Auto appena ripubblicata: ogni avviso non inviato con soglia >= prezzo ha mancato l'attraversamento
     * mentre l'auto era in bozza (alla creazione la soglia e' sempre sotto il prezzo).
     */
    @Query("""
            SELECT a.id FROM Avviso a
            WHERE a.auto.id = :autoId
              AND a.inviato = false
              AND a.soglia >= :nuovo
            """)
    List<UUID> daInviareAllaPubblicazione(UUID autoId, BigDecimal nuovo);

    /**
     * Prende il segno "inviato" in un colpo solo. Restituisce 1 solo al primo che ci arriva:
     * se due cambi di prezzo ravvicinati scattano insieme, il secondo trova inviato = true e ottiene 0.
     * Nello stesso UPDATE si salva l'hash del token per il link "disattiva".
     */
    @Modifying
    @Query("""
            UPDATE Avviso a SET a.inviato = true, a.tokenDisattivaHash = :hash
            WHERE a.id = :id AND a.inviato = false
            """)
    int prendiInCarico(UUID id, String hash);

    @Modifying
    @Query("DELETE FROM Avviso a WHERE a.utente.id = :utenteId")
    void eliminaDiUtente(UUID utenteId);
}
