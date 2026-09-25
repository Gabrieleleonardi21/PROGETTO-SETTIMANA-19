# Salone Auto - BE + FE (JSX) + PostgreSQL

Mini salone di automobili (Consegna D5). Chi non ha fatto l'accesso sfoglia le auto pubblicate con ricerca e
ordinamento; l'utente registrato salva i preferiti e fissa una soglia di prezzo; l'amministratore vede bozze e prezzo
d'acquisto, crea e modifica le auto e ne cambia il prezzo. Quando il prezzo scende sotto la soglia parte **una** mail.

Le scelte di progetto sono spiegate in `RELAZIONE.txt`.

| Parte | Tecnologia | In locale | Su Render |
|---|---|---|---|
| Backend | Spring Boot 4.1.1, Java 25, Spring Security (JWT), Mail + Thymeleaf | `be` sulla 8080 | Web Service (Docker) |
| Frontend | React 19, Vite, JSX, Tailwind 4, shadcn, TanStack Query | `fe` sulla 5173 | Static Site |
| Database | PostgreSQL | locale sulla 5432 | Render PostgreSQL |

## Endpoint

| Metodo | Percorso | Chi |
|---|---|---|
| POST | `/api/auth/registrati`, `/api/auth/login` | pubblico |
| GET | `/api/auto?q=&prezzoMin=&prezzoMax=&sort=prezzo,asc&page=0&size=12` | pubblico (solo pubblicate) |
| GET | `/api/auto/{id}` | pubblico (bozza = 404) |
| GET / PUT / DELETE | `/api/me` | utente (DELETE = elimina account, avvisi e preferiti) |
| GET / POST · DELETE | `/api/preferiti` · `/api/preferiti/{id}` | utente |
| GET / POST · DELETE | `/api/avvisi` · `/api/avvisi/{id}` | utente |
| POST | `/api/avvisi/disattiva` `{ "token": "..." }` | pubblico, token monouso dalla mail |
| GET / POST · GET / PUT | `/api/admin/auto` · `/api/admin/auto/{id}` | admin |
| PATCH | `/api/admin/auto/{id}/prezzo` `{ "prezzo": 9500, "versione": 3 }` | admin |
| GET | `/actuator/health` | pubblico (health check di Render) |

Ordinamenti ammessi (`sort`): `prezzo`, `anno`, `km`, `marca`, `recenti`. Qualsiasi altro campo risponde 400.

Catalogo di esempio: al primo avvio, se non ci sono auto, il backend importa 18 modelli europei con la foto
dall'API di Wikipedia e le 5 auto di DummyJSON con la galleria (`CatalogoEsempio`). Si spegne con
`IMPORTA_AUTO_ESEMPIO=false`. Le foto sono ammesse solo in https da `upload.wikimedia.org` e `cdn.dummyjson.com`.

Frontend: tema chiaro/scuro (next-themes), animazioni (Motion), galleria (carosello shadcn/Embla), hero 3D
(Three.js con @react-three/fiber, caricato solo in home), toggle del tema e scheda con inclinazione adattati da 21st.dev.

Login e registrazione: massimo 10 richieste al minuto per IP (`AUTH_TENTATIVI_AL_MINUTO`), poi 429.
Modifica e cambio prezzo richiedono la `versione` dell'auto letta prima: se nel frattempo è cambiata, 409.

## Avvio in locale

1. PostgreSQL sulla 5432 e database creato:
   ```
   createdb -U postgres salone_auto
   ```
   Credenziali diverse da `postgres` / `admin`: variabili `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
2. Segreti: copiare `be/.env.example` in `be/.env` (ignorato da git) e compilare
   `JWT_SECRET` (almeno 32 caratteri), `ADMIN_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`
   (password per le app di Gmail). Senza questi valori il backend non parte.
3. Doppio clic su `avvia.cmd` (Windows) o `./avvia.sh` (macOS/Linux), oppure:
   ```
   cd be && ./mvnw spring-boot:run
   cd fe && npm install && npm run dev
   ```
4. http://localhost:5173 - l'amministratore è `admin@salone.it` (o `ADMIN_EMAIL`) con la password di `ADMIN_PASSWORD`.

## Deploy su Render

1. Repository Git con `be/`, `fe/`, `render.yaml` nella radice.
2. **New > Blueprint**, si sceglie la repo: nascono `salone-db`, `salone-be`, `salone-fe`.
   `JWT_SECRET` la genera Render.
3. Dopo la prima build si impostano le variabili `sync: false`, senza `/` finale:

   | Servizio | Variabile | Valore |
   |---|---|---|
   | `salone-be` | `ALLOWED_ORIGIN` | `https://salone-fe.onrender.com` |
   | `salone-be` | `FRONTEND_URL` | `https://salone-fe.onrender.com` |
   | `salone-be` | `ADMIN_PASSWORD` | password dell'amministratore |
   | `salone-be` | `MAIL_USERNAME` | indirizzo Gmail |
   | `salone-be` | `MAIL_PASSWORD` | password per le app di Gmail |
   | `salone-fe` | `VITE_API_URL` | `https://salone-be.onrender.com` |

4. **Manual Deploy** di entrambi (`VITE_API_URL` è letta in fase di build).

## Struttura

```
render.yaml                 blueprint: database + backend + frontend
RELAZIONE.txt               scelte di progetto (avvisi, mail, attacchi, privacy)
be/src/main/java/it/epicode/salone/
  config/      DatabaseUrl (DATABASE_URL -> JDBC), DataInitializer (admin all'avvio)
  security/    SecurityConfig (JWT, CORS, ruoli), TokenHasher, UtenteCorrente, LimiteAuthFilter (429)
  entities/    Utente, Auto, Preferito, Avviso
  dto/         record in ingresso e in uscita (mai entità nei controller)
  services/    Auto, Utente, Preferito, Avviso, Email, Jwt, ricerca con Specification
  events/      PrezzoCambiatoEvent + AvvisoListener (AFTER_COMMIT + @Async)
  controllers/ REST
be/src/main/resources/templates/email/avviso-prezzo.html
fe/src/
  lib/         api.js (fetch + Bearer), sessione.js (localStorage), endpoints.js
  features/    catalogo, auth, preferiti, avvisi, profilo, admin, legale (Privacy e Cookie)
```

## Fonti e crediti

Elenco completo anche nell'app, alla pagina **Crediti e fonti** (link nel footer).

| Cosa | Fonte |
|---|---|
| Template di partenza | cartella `HELP/Deploy-Base-JSX` della repo del docente, [JavaAIUnit5](https://github.com/MatteoPattavinaDocente/JavaAIUnit5) |
| Foto dei 18 modelli europei | [Wikimedia Commons](https://commons.wikimedia.org), trovate con l'[API di Wikipedia](https://www.mediawiki.org/wiki/API:Main_page). Autore e licenza (CC BY-SA 3.0/4.0, GFDL) di ogni foto sono letti dall'API di Commons e mostrati sotto la foto con il link al file |
| 5 auto con galleria | [DummyJSON](https://dummyjson.com/docs/products), API di dati fittizi |
| Anno, km, prezzi, descrizioni | **inventati** a scopo didattico (marche e modelli sono reali) |
| Toggle del tema | [Animated Theme Toggle](https://21st.dev/@johuniq/components/animated-theme-toggle) di johuniq, 21st.dev (adattato) |
| Inclinazione delle schede | [Product Card](https://21st.dev/@ravikatiyar162/components/product-card) di ravikatiyar162, 21st.dev (adattato) |
| Componenti UI | [shadcn/ui](https://ui.shadcn.com) su Radix UI; parti riprese dal progetto U5D13 del corso |
| Librerie | React, React Router, TanStack Query, React Hook Form, Zod, Tailwind, Motion, Embla, next-themes, Sonner, Lucide, Three.js + React Three Fiber + Drei; Spring Boot, Hibernate, Thymeleaf, Lombok, PostgreSQL |
