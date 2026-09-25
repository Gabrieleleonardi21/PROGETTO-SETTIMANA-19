package it.epicode.salone.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Controllo degli indirizzi delle foto e della loro fonte.
 * Solo https e solo da un elenco chiuso di host: la pagina li carica in <img> e in un link, e la
 * Content-Security-Policy del frontend ammette immagini solo da questi stessi host.
 * Niente javascript:, data: o server arbitrari che potrebbero tracciare chi guarda il catalogo.
 */
final class ImmaginiAuto {

    static final Set<String> HOST_FOTO = Set.of("upload.wikimedia.org", "cdn.dummyjson.com");
    static final Set<String> HOST_FONTE = Set.of("commons.wikimedia.org", "en.wikipedia.org", "it.wikipedia.org", "dummyjson.com");

    private ImmaginiAuto() {
    }

    static List<String> foto(List<String> urls) {
        if (urls == null) {
            return List.of();
        }
        return urls.stream().map(String::trim).filter(u -> !u.isEmpty()).map(u -> valida(u, HOST_FOTO)).toList();
    }

    static String fonte(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return valida(url.trim(), HOST_FONTE);
    }

    private static String valida(String url, Set<String> hostAmmessi) {
        try {
            URI uri = URI.create(url);
            if ("https".equals(uri.getScheme()) && hostAmmessi.contains(uri.getHost())) {
                return url;
            }
        } catch (IllegalArgumentException e) {
            // URL malformato: stesso errore di un host non ammesso
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Indirizzo non ammesso: servono https e uno di questi siti " + hostAmmessi);
    }
}
