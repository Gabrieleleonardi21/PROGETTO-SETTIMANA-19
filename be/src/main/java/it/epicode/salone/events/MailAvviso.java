package it.epicode.salone.events;

import java.math.BigDecimal;

// Dati gia' pronti per la mail, letti dentro la transazione della presa in carico
public record MailAvviso(String email, String nome, String marca, String modello, BigDecimal prezzo,
                         BigDecimal soglia, String tokenDisattiva) {
}
