package it.epicode.salone.repositories;

import it.epicode.salone.entities.Preferito;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PreferitoRepository extends JpaRepository<Preferito, UUID> {

    // Solo le auto ancora pubblicate: se l'admin la rimette in bozza sparisce dalla lista
    @EntityGraph(attributePaths = "auto")
    List<Preferito> findByUtenteIdAndAutoPubblicataTrueOrderByCreatoIlDesc(UUID utenteId);

    // Cerca per id E proprietario: il preferito di un altro utente risulta inesistente (404)
    Optional<Preferito> findByIdAndUtenteId(UUID id, UUID utenteId);

    boolean existsByUtenteIdAndAutoId(UUID utenteId, UUID autoId);

    @Modifying
    @Query("DELETE FROM Preferito p WHERE p.utente.id = :utenteId")
    void eliminaDiUtente(UUID utenteId);
}
