package it.epicode.salone.controllers;

import it.epicode.salone.dto.LoginRequest;
import it.epicode.salone.dto.LoginResponse;
import it.epicode.salone.dto.RegistrazioneRequest;
import it.epicode.salone.services.UtenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtenteService utenteService;

    @PostMapping("/registrati")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registrati(@RequestBody @Valid RegistrazioneRequest req) {
        return utenteService.registra(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest req) {
        return utenteService.login(req);
    }
}
