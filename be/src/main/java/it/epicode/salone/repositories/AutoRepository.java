package it.epicode.salone.repositories;

import it.epicode.salone.entities.Auto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AutoRepository extends JpaRepository<Auto, UUID>, JpaSpecificationExecutor<Auto> {

    // Lato pubblico: una bozza risponde come un'auto che non esiste
    Optional<Auto> findByIdAndPubblicataTrue(UUID id);
}
