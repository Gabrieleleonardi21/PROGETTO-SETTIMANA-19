package it.epicode.salone.config;

import tools.jackson.databind.JsonNode;
import it.epicode.salone.entities.Alimentazione;
import it.epicode.salone.entities.Auto;
import it.epicode.salone.repositories.AutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Al primo avvio, se il catalogo e' vuoto, lo riempie con auto di esempio:
 * - 18 modelli europei: anno, km, prezzi e descrizioni inventati per l'esercizio; foto dall'API di Wikipedia
 *   (Wikimedia Commons) con autore e licenza letti dall'API di Commons;
 * - le 5 auto dell'API DummyJSON, con la loro galleria di foto.
 * Gira in un altro thread dopo l'avvio: l'app risponde subito e, se internet non c'e', parte lo stesso.
 * Si spegne con IMPORTA_AUTO_ESEMPIO=false.
 */
@Slf4j
@Component
public class CatalogoEsempio {

    // Wikipedia chiede un User-Agent che identifichi l'applicazione
    private static final String USER_AGENT = "SaloneAuto/1.0 (progetto didattico Epicode)";
    // Cambio indicativo per i prezzi DummyJSON, che sono in dollari
    private static final BigDecimal USD_EUR = new BigDecimal("0.92");

    // titoloWikipedia, marca, modello, anno, km, alimentazione, prezzo, prezzo d'acquisto, descrizione
    private record Modello(String pagina, String marca, String modello, int anno, int km, Alimentazione alimentazione,
                           int prezzo, int prezzoAcquisto, String descrizione) {
    }

    private static final List<Modello> MODELLI = List.of(
            new Modello("Fiat_Panda", "Fiat", "Panda", 2019, 42_000, Alimentazione.BENZINA, 9_900, 7_600,
                    "1.2 Easy, cinque porte, perfetta in città. Tagliandi regolari, unico proprietario."),
            new Modello("Fiat_500_(2007)", "Fiat", "500", 2018, 55_000, Alimentazione.BENZINA, 10_500, 8_200,
                    "1.2 Lounge con tetto panoramico in vetro e climatizzatore automatico."),
            new Modello("Volkswagen_Golf_Mk8", "Volkswagen", "Golf", 2021, 38_000, Alimentazione.BENZINA, 22_900, 19_000,
                    "1.5 eTSI Style con cambio DSG, fari LED e cockpit digitale."),
            new Modello("Toyota_Yaris", "Toyota", "Yaris", 2021, 29_000, Alimentazione.IBRIDA, 18_400, 15_300,
                    "Hybrid 1.5 Trend: consumi bassissimi, ideale per chi guida molto in città."),
            new Modello("Renault_Clio", "Renault", "Clio", 2022, 24_000, Alimentazione.GPL, 15_900, 12_900,
                    "1.0 TCe GPL di fabbrica: doppia alimentazione e bollo ridotto."),
            new Modello("Peugeot_208", "Peugeot", "208", 2022, 31_000, Alimentazione.BENZINA, 17_500, 14_400,
                    "PureTech 130 GT Line con cambio automatico EAT8 e i-Cockpit 3D."),
            new Modello("Alfa_Romeo_Giulia_(2015)", "Alfa Romeo", "Giulia", 2020, 61_000, Alimentazione.DIESEL, 27_800, 23_500,
                    "2.2 Turbodiesel 190 CV Veloce, trazione posteriore, interni in pelle."),
            new Modello("Tesla_Model_3", "Tesla", "Model 3", 2023, 18_000, Alimentazione.ELETTRICA, 33_900, 29_000,
                    "Long Range a trazione integrale, autonomia oltre 500 km, Autopilot incluso."),
            new Modello("Volkswagen_Polo_Mk6", "Volkswagen", "Polo", 2019, 47_000, Alimentazione.BENZINA, 13_200, 10_700,
                    "1.0 TSI Comfortline, sensori di parcheggio e Apple CarPlay."),
            new Modello("Dacia_Sandero", "Dacia", "Sandero", 2023, 12_000, Alimentazione.GPL, 13_900, 11_500,
                    "Stepway 1.0 ECO-G bifuel: spaziosa, economica e quasi nuova."),
            new Modello("Jeep_Renegade", "Jeep", "Renegade", 2020, 58_000, Alimentazione.DIESEL, 18_900, 15_600,
                    "1.6 MultiJet Longitude, posizione di guida alta e bagagliaio capiente."),
            new Modello("Fiat_Tipo_(2015)", "Fiat", "Tipo", 2022, 35_000, Alimentazione.BENZINA, 15_400, 12_600,
                    "1.0 Firefly Life, berlina comoda per la famiglia."),
            new Modello("BMW_3_Series_(G20)", "BMW", "Serie 3", 2020, 72_000, Alimentazione.DIESEL, 26_500, 22_300,
                    "318d Business Advantage, navigatore e cambio automatico a 8 rapporti."),
            new Modello("Audi_A3", "Audi", "A3", 2021, 40_000, Alimentazione.BENZINA, 25_900, 21_800,
                    "Sedan 35 TFSI S tronic, Virtual Cockpit e luci Matrix LED."),
            new Modello("Ford_Puma_(crossover)", "Ford", "Puma", 2021, 33_000, Alimentazione.IBRIDA, 19_800, 16_500,
                    "1.0 EcoBoost Hybrid Titanium, MegaBox nel bagagliaio."),
            new Modello("Lancia_Ypsilon", "Lancia", "Ypsilon", 2024, 6_000, Alimentazione.ELETTRICA, 26_900, 23_000,
                    "Nuova Ypsilon elettrica, praticamente nuova, garanzia ufficiale."),
            new Modello("Toyota_C-HR", "Toyota", "C-HR", 2024, 9_000, Alimentazione.IBRIDA, 31_500, 27_200,
                    "Hybrid 1.8 Trend, linea da coupé e consumi da utilitaria."),
            new Modello("Nissan_Qashqai", "Nissan", "Qashqai", 2023, 21_000, Alimentazione.IBRIDA, 27_400, 23_300,
                    "e-Power N-Connecta: guida elettrica senza ricaricare la batteria."));

    // DummyJSON ha le descrizioni in inglese: per le sue 5 auto si usa un testo italiano, se lo conosciamo
    private static final Map<String, String> DESCRIZIONI_DUMMY = Map.of(
            "300 Touring", "Berlina americana di rappresentanza, V6 3.6 e interni ampi.",
            "Charger SXT RWD", "Muscle car a trazione posteriore, V6 3.6 da 300 CV.",
            "Dodge Hornet GT Plus", "SUV compatto sportivo, motore turbo e dotazione completa.",
            "Durango SXT RWD", "SUV a sette posti, spazio per tutta la famiglia e i bagagli.",
            "Pacifica Touring", "Monovolume a sette posti con porte scorrevoli elettriche.");

    private final AutoRepository autoRepository;
    private final boolean attivo;
    private final RestClient http;

    public CatalogoEsempio(AutoRepository autoRepository, @Value("${app.importa-auto-esempio}") boolean attivo) {
        this.autoRepository = autoRepository;
        this.attivo = attivo;
        // Timeout brevi: un'API lenta non deve tenere occupato il thread per minuti
        SimpleClientHttpRequestFactory timeout = new SimpleClientHttpRequestFactory();
        timeout.setConnectTimeout(Duration.ofSeconds(5));
        timeout.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder()
                .requestFactory(timeout)
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void importaSeVuoto() {
        if (!attivo || autoRepository.count() > 0) {
            return;
        }
        Map<String, FotoWiki> foto = fotoDaWikipedia();
        int importate = 0;
        for (Modello m : MODELLI) {
            importate += salva(daModello(m, foto.get(m.pagina())));
        }
        for (Auto auto : daDummyJson()) {
            importate += salva(auto);
        }
        log.info("Catalogo di esempio: {} auto importate, {} con foto da Wikipedia", importate, foto.size());
    }

    // Un errore su un'auto non ferma le altre
    private int salva(Auto auto) {
        try {
            autoRepository.save(auto);
            return 1;
        } catch (RuntimeException e) {
            log.warn("Auto di esempio {} {} non salvata: {}", auto.getMarca(), auto.getModello(), e.getClass().getSimpleName());
            return 0;
        }
    }

    // Foto di una voce di Wikipedia con i crediti richiesti dalla sua licenza
    private record FotoWiki(String url, String crediti, String fonte) {
    }

    private Auto daModello(Modello m, FotoWiki foto) {
        Auto auto = nuova(m.marca(), m.modello(), m.anno(), m.km(), m.alimentazione(), m.descrizione(),
                BigDecimal.valueOf(m.prezzo()), BigDecimal.valueOf(m.prezzoAcquisto()));
        // Senza foto l'auto si importa lo stesso: il frontend mostra un segnaposto
        if (foto != null) {
            auto.getImmagini().add(foto.url());
            auto.setCreditiFoto(foto.crediti());
            auto.setFonteFoto(foto.fonte());
        }
        return auto;
    }

    /**
     * Foto e crediti di tutti i modelli con due sole richieste (le API "action" accettano piu' titoli insieme):
     * 1. Wikipedia: per ogni voce la foto principale a 960px e il nome del file su Commons;
     * 2. Commons: per ogni file autore e licenza, che le licenze CC BY / CC BY-SA / GFDL chiedono di citare.
     * Una richiesta per auto farebbe scattare il limite di Wikimedia (429 Too Many Requests).
     * Restituisce una mappa titolo della voce -> foto; se le API non rispondono e' vuota.
     */
    private Map<String, FotoWiki> fotoDaWikipedia() {
        try {
            String titoli = MODELLI.stream().map(Modello::pagina).collect(Collectors.joining("|"));
            JsonNode wiki = conRiprova("https://en.wikipedia.org/w/api.php?action=query&format=json&formatversion=2"
                    + "&prop=pageimages&piprop=thumbnail|name&pithumbsize=960&redirects=1&titles={titoli}", titoli);

            // Wikipedia normalizza i titoli ("Fiat_Panda" -> "Fiat Panda") e segue i redirect: si ricostruisce il percorso
            Map<String, String> alias = new HashMap<>();
            wiki.path("query").path("normalized").valueStream().forEach(n -> alias.put(n.path("from").asString(), n.path("to").asString()));
            wiki.path("query").path("redirects").valueStream().forEach(n -> alias.put(n.path("from").asString(), n.path("to").asString()));
            Map<String, JsonNode> pagine = new HashMap<>();
            wiki.path("query").path("pages").valueStream().forEach(p -> pagine.put(p.path("title").asString(), p));

            Map<String, JsonNode> perModello = new LinkedHashMap<>();
            for (Modello m : MODELLI) {
                JsonNode pagina = pagine.get(risolvi(m.pagina(), alias));
                if (pagina != null && !pagina.path("pageimage").asString("").isEmpty()) {
                    perModello.put(m.pagina(), pagina);
                }
            }

            Map<String, String[]> attribuzioni = attribuzioniCommons(perModello.values().stream()
                    .map(p -> p.path("pageimage").asString()).toList());

            Map<String, FotoWiki> risultato = new HashMap<>();
            perModello.forEach((titolo, pagina) -> {
                String url = fotoSicura(pagina.path("thumbnail").path("source").asString(""));
                if (url.isEmpty()) {
                    return;
                }
                // Senza dati da Commons si cita comunque l'archivio e si rimanda alla voce di Wikipedia
                String[] attr = attribuzioni.getOrDefault(pagina.path("pageimage").asString(), new String[]{
                        "Foto: Wikimedia Commons (autore e licenza nella pagina della fonte)",
                        "https://en.wikipedia.org/wiki/" + URLEncoder.encode(titolo, StandardCharsets.UTF_8)});
                risultato.put(titolo, new FotoWiki(url, attr[0], attr[1]));
            });
            return risultato;
        } catch (RestClientException e) {
            log.warn("Wikipedia non raggiungibile ({}): auto importate senza foto", e.getClass().getSimpleName());
            return Map.of();
        }
    }

    private static String risolvi(String titolo, Map<String, String> alias) {
        String attuale = titolo;
        // Al massimo due passaggi: normalizzazione e poi redirect
        for (int i = 0; i < 2 && alias.containsKey(attuale); i++) {
            attuale = alias.get(attuale);
        }
        return attuale;
    }

    /** Nome del file -> { "Foto: autore, licenza, via Wikimedia Commons", pagina del file su Commons }. */
    private Map<String, String[]> attribuzioniCommons(List<String> file) {
        if (file.isEmpty()) {
            return Map.of();
        }
        String titoli = file.stream().map(f -> "File:" + f).collect(Collectors.joining("|"));
        JsonNode commons = conRiprova("https://commons.wikimedia.org/w/api.php?action=query&format=json&formatversion=2"
                + "&prop=imageinfo&iiprop=extmetadata|url&titles={titoli}", titoli);

        Map<String, String[]> risultato = new HashMap<>();
        commons.path("query").path("pages").valueStream().forEach(p -> {
            JsonNode info = p.path("imageinfo").path(0);
            JsonNode meta = info.path("extmetadata");
            String autore = testoSemplice(meta.path("Artist").path("value").asString(""));
            String licenza = testoSemplice(meta.path("LicenseShortName").path("value").asString(""));
            String pagina = info.path("descriptionurl").asString("");
            if (autore.isEmpty() || licenza.isEmpty() || !pagina.startsWith("https://commons.wikimedia.org/")) {
                return;
            }
            // Commons risponde "File:2018 Fiat Panda.jpg", Wikipedia aveva dato "2018_Fiat_Panda.jpg"
            String nome = p.path("title").asString().replaceFirst("^File:", "").replace(' ', '_');
            risultato.put(nome, new String[]{tronca("Foto: " + autore + ", " + licenza + ", via Wikimedia Commons", 200), pagina});
        });
        return risultato;
    }

    /** GET con al massimo tre tentativi: su 429 si aspetta quanto chiede l'header Retry-After (max 10 s). */
    private JsonNode conRiprova(String uri, Object... variabili) {
        for (int tentativo = 1; ; tentativo++) {
            try {
                return http.get().uri(uri, variabili).retrieve().body(JsonNode.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (tentativo == 3) {
                    throw e;
                }
                attendi(e.getResponseHeaders());
            }
        }
    }

    private static void attendi(HttpHeaders intestazioni) {
        long secondi = 2;
        if (intestazioni != null && intestazioni.getFirst(HttpHeaders.RETRY_AFTER) != null) {
            try {
                secondi = Math.min(10, Long.parseLong(intestazioni.getFirst(HttpHeaders.RETRY_AFTER).trim()));
            } catch (NumberFormatException e) {
                // Retry-After in formato data: si tiene l'attesa predefinita
            }
        }
        try {
            Thread.sleep(Duration.ofSeconds(secondi));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Commons restituisce l'autore come HTML (un link al profilo): si tengono solo le parole, mai il markup
    private static String testoSemplice(String html) {
        String senzaTag = html.replaceAll("<[^>]*>", " ");
        return HtmlUtils.htmlUnescape(senzaTag).replaceAll("\\s+", " ").trim();
    }

    private static String tronca(String testo, int massimo) {
        if (testo.length() <= massimo) {
            return testo;
        }
        return testo.substring(0, massimo - 1) + "…";
    }

    /**
     * L'API restituisce la foto a 960px con parametri di tracciamento, a volte su thumb.wikimedia.org:
     * si tolgono i parametri e si usa sempre upload.wikimedia.org, l'unico host Wikimedia ammesso dalla CSP.
     */
    private static String fotoSicura(String url) {
        String pulito = url.split("\\?", 2)[0].replace("https://thumb.wikimedia.org/", "https://upload.wikimedia.org/");
        if (!pulito.startsWith("https://upload.wikimedia.org/")) {
            return "";
        }
        return pulito;
    }

    private List<Auto> daDummyJson() {
        try {
            JsonNode risposta = http.get()
                    .uri("https://dummyjson.com/products/category/vehicle?limit=0")
                    .retrieve()
                    .body(JsonNode.class);
            return risposta.path("products").valueStream().map(CatalogoEsempio::daProdotto).toList();
        } catch (RestClientException e) {
            log.warn("DummyJSON non raggiungibile ({}): importate solo le auto da Wikipedia", e.getClass().getSimpleName());
            return List.of();
        }
    }

    private static Auto daProdotto(JsonNode p) {
        String titolo = p.path("title").asString();
        String marca = p.path("brand").asString();
        // "Dodge Hornet GT Plus": il modello non ripete la marca
        String modello = titolo.replaceFirst("^" + java.util.regex.Pattern.quote(marca) + "\\s+", "");
        BigDecimal prezzo = BigDecimal.valueOf(p.path("price").asDouble()).multiply(USD_EUR)
                .setScale(-2, RoundingMode.HALF_UP).setScale(2, RoundingMode.UNNECESSARY);
        // Anno e km non sono nell'API: valori plausibili ricavati dal prezzo, sempre uguali a ogni import
        int anno = 2019 + (prezzo.intValue() / 1000) % 5;
        int km = 15_000 + (prezzo.intValue() % 7) * 9_000;
        String descrizione = DESCRIZIONI_DUMMY.getOrDefault(titolo, p.path("description").asString(""));

        Auto auto = nuova(marca, modello, anno, km, Alimentazione.BENZINA, descrizione, prezzo,
                prezzo.multiply(new BigDecimal("0.82")).setScale(-2, RoundingMode.HALF_UP).setScale(2, RoundingMode.UNNECESSARY));
        List<String> foto = p.path("images").valueStream()
                .map(JsonNode::asString)
                .filter(url -> url.startsWith("https://cdn.dummyjson.com/"))
                .limit(8)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        // La terza foto e' la vista laterale, che riempie bene una copertina orizzontale: va per prima
        if (foto.size() >= 3) {
            foto.addFirst(foto.remove(2));
        }
        auto.getImmagini().addAll(foto);
        auto.setCreditiFoto("Foto e modello: DummyJSON (dummyjson.com), API di dati fittizi per sviluppatori");
        auto.setFonteFoto("https://dummyjson.com/docs/products");
        return auto;
    }

    private static Auto nuova(String marca, String modello, int anno, int km, Alimentazione alimentazione,
                              String descrizione, BigDecimal prezzo, BigDecimal prezzoAcquisto) {
        Auto auto = new Auto();
        auto.setMarca(marca);
        auto.setModello(modello);
        auto.setAnno(anno);
        auto.setKm(km);
        auto.setAlimentazione(alimentazione);
        auto.setDescrizione(descrizione);
        auto.setPrezzo(prezzo);
        auto.setPrezzoAcquisto(prezzoAcquisto);
        auto.setPubblicata(true);
        return auto;
    }
}
