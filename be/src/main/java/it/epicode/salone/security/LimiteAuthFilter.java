package it.epicode.salone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;

/**
 * Freno contro gli attacchi a dizionario: login e registrazione accettano al massimo
 * app.auth.tentativi-al-minuto richieste al minuto per IP, poi rispondono 429.
 * L'IP e' quello vero del client anche dietro il proxy di Render (forward-headers-strategy: framework).
 */
@Slf4j
@Component
public class LimiteAuthFilter extends OncePerRequestFilter {

    private final LimiteTentativi limite;

    public LimiteAuthFilter(@Value("${app.auth.tentativi-al-minuto}") int tentativiAlMinuto) {
        this.limite = new LimiteTentativi(tentativiAlMinuto, Clock.systemUTC());
    }

    // Solo le POST di login e registrazione; tutto il resto passa senza conteggio
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        if (limite.consenti(ip)) {
            chain.doFilter(request, response);
            return;
        }
        // Nel log niente IP completo ne' email: basta sapere che il freno e' scattato
        log.warn("Troppi tentativi su {}", request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(limite.secondiAttesa(ip)));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":429,\"message\":\"Troppi tentativi: riprova tra un minuto.\"}");
    }
}
