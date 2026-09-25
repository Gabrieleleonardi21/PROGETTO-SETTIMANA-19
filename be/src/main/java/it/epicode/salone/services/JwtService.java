package it.epicode.salone.services;

import it.epicode.salone.dto.LoginResponse;
import it.epicode.salone.dto.UtenteResponse;
import it.epicode.salone.entities.Utente;
import it.epicode.salone.security.SecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    // Nel token solo quello che serve al server: id (subject), ruolo e scadenza. Niente email ne' nome.
    public LoginResponse emetti(Utente utente) {
        Instant adesso = Instant.now();
        Instant scadenza = adesso.plus(expirationMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(utente.getId().toString())
                .issuedAt(adesso)
                .expiresAt(scadenza)
                .claim(SecurityConfig.CLAIM_RUOLO, List.of(utente.getRuolo().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponse(token, scadenza, UtenteResponse.da(utente));
    }
}
