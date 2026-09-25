package it.epicode.salone.services;

import it.epicode.salone.events.MailAvviso;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Mail HTML generata dal template templates/email/avviso-prezzo.html.
 * Nel template ogni valore passa da th:text / th:href, che fanno l'escape: un nome utente come
 * "<script>" arriva nella mail come testo, non come markup.
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String mittente;
    private final String frontendUrl;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
                        @Value("${spring.mail.username}") String mittente,
                        @Value("${app.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.mittente = mittente;
        this.frontendUrl = frontendUrl.replaceAll("/$", "");
    }

    /**
     * Scelta sul fallimento di Gmail: l'avviso resta "inviato" e la mail e' persa.
     * Nessun rilancio, cosi' non si rischia mai un doppione. Nel log solo l'id dell'avviso.
     */
    public void inviaAvvisoPrezzo(UUID avvisoId, MailAvviso dati) {
        String link = frontendUrl + "/avvisi/disattiva?token="
                + URLEncoder.encode(dati.tokenDisattiva(), StandardCharsets.UTF_8);

        Context ctx = new Context();
        ctx.setVariable("nome", dati.nome());
        ctx.setVariable("auto", dati.marca() + " " + dati.modello());
        ctx.setVariable("prezzo", dati.prezzo());
        ctx.setVariable("soglia", dati.soglia());
        ctx.setVariable("link", link);

        try {
            String html = templateEngine.process("email/avviso-prezzo", ctx);
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, StandardCharsets.UTF_8.name());
            helper.setFrom(mittente);
            helper.setTo(dati.email());
            helper.setSubject("Il prezzo è sceso sotto la tua soglia");
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Mail avviso {} inviata", avvisoId);
        } catch (MailException | MessagingException | TemplateProcessingException e) {
            // Solo il tipo di errore: il messaggio di Gmail potrebbe contenere l'indirizzo del destinatario
            log.warn("Invio mail avviso {} fallito ({}): l'avviso resta inviato", avvisoId, e.getClass().getSimpleName());
        }
    }
}
