package it.epicode.salone.dto;

import java.math.BigDecimal;

// Filtri facoltativi del catalogo, letti dalla query string
public record AutoSearchParams(
        String q,              // testo libero su marca e modello
        BigDecimal prezzoMin,
        BigDecimal prezzoMax
) {
}
