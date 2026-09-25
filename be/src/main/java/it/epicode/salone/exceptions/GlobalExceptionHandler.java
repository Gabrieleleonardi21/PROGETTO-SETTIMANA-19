package it.epicode.salone.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

// Al client solo messaggi scritti da noi; i dettagli delle eccezioni inattese restano nel log del server
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 con l'elenco dei campi non validi: { "errors": { "email": "...", ... } }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validazione(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError e : ex.getBindingResult().getFieldErrors()) {
            // Errore di conversione (es. "abc" in un campo numerico): il testo di Spring rivela i tipi Java
            String messaggio = e.getDefaultMessage();
            if (e.isBindingFailure()) {
                messaggio = "Valore non valido";
            }
            errors.putIfAbsent(e.getField(), messaggio);
        }
        return Map.of("status", 400, "errors", errors);
    }

    // JSON malformato o id non UUID
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> richiestaNonValida(Exception ex) {
        return Map.of("status", 400, "message", "Richiesta non valida");
    }

    // Errori di business (404, 409, ...) con il messaggio scritto nel service
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> stato(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        String message = ex.getReason();
        if (message == null) {
            message = HttpStatus.valueOf(status).getReasonPhrase();
        }
        return ResponseEntity.status(status).body(Map.of("status", status, "message", message));
    }

    // @PreAuthorize negato: si rilancia perche' lo trasformi Spring Security in 403/401
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public void sicurezza(RuntimeException ex) {
        throw ex;
    }

    // Stessa auto modificata da due amministratori insieme (@Version)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> concorrenza(ObjectOptimisticLockingFailureException ex) {
        return Map.of("status", 409, "message", "L'auto è stata appena modificata da un altro amministratore: ricarica.");
    }

    // Vincolo unique violato da due richieste contemporanee (es. doppio clic su "preferito")
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> duplicato(DataIntegrityViolationException ex) {
        return Map.of("status", 409, "message", "Elemento già presente");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inatteso(Exception ex) {
        // Eccezioni di Spring MVC con uno stato gia' deciso (404 indirizzo inesistente, 405 metodo sbagliato...)
        if (ex instanceof ErrorResponse er) {
            int status = er.getStatusCode().value();
            return ResponseEntity.status(status).body(Map.of("status", status, "message", "Richiesta non valida"));
        }
        log.error("Errore inatteso", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", 500, "message", "Errore interno, riprova più tardi"));
    }
}
