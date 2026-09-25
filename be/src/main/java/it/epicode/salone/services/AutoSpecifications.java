package it.epicode.salone.services;

import it.epicode.salone.dto.AutoSearchParams;
import it.epicode.salone.entities.Auto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static it.epicode.salone.services.SearchUtils.like;
import static it.epicode.salone.services.SearchUtils.presente;

// Filtri del catalogo costruiti con la Criteria API: nessuna stringa SQL concatenata, i valori sono parametri
final class AutoSpecifications {

    private AutoSpecifications() {
    }

    static Specification<Auto> pubbliche(AutoSearchParams p) {
        return (root, query, cb) -> {
            List<Predicate> filtri = new ArrayList<>();
            filtri.add(cb.isTrue(root.get("pubblicata")));

            if (presente(p.q())) {
                filtri.add(cb.or(like(cb, root.get("marca"), p.q()), like(cb, root.get("modello"), p.q())));
            }
            if (p.prezzoMin() != null) {
                filtri.add(cb.greaterThanOrEqualTo(root.get("prezzo"), p.prezzoMin()));
            }
            if (p.prezzoMax() != null) {
                filtri.add(cb.lessThanOrEqualTo(root.get("prezzo"), p.prezzoMax()));
            }
            return cb.and(filtri.toArray(Predicate[]::new));
        };
    }
}
