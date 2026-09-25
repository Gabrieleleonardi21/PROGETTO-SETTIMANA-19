import { Link } from 'react-router'
import { Informativa } from './Informativa'

// Elenca quello che resta davvero nel browser: solo la chiave salone.sessione (lib/sessione.js)
export function CookiePage() {
  return (
    <Informativa titolo="Cookie Policy" aggiornata="25 settembre 2026">
      <p>
        Salone Auto non usa cookie di profilazione, di statistica o di terze parti e non mostra pubblicità. Per questo non
        ti chiediamo un consenso con il banner.
      </p>

      <h2>Cosa resta nel tuo browser</h2>
      <table className="w-full text-sm">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Dove</th>
            <th>Cosa contiene</th>
            <th>Durata</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>salone.sessione</td>
            <td>localStorage</td>
            <td>
              Il token di accesso (JWT) e la sua scadenza. Il token contiene solo il tuo identificativo (un codice casuale) e il ruolo,
              non email né nome.
            </td>
            <td>Fino all'uscita o alla scadenza del token (2 ore). Si cancella quando premi «Esci».</td>
          </tr>
          <tr>
            <td>salone.tema</td>
            <td>localStorage</td>
            <td>La tua scelta tra tema chiaro e scuro («light», «dark» o «system»). Nessun dato personale.</td>
            <td>Finché non cancelli i dati del sito.</td>
          </tr>
        </tbody>
      </table>

      <h2>Perché servono</h2>
      <p>
        Il token è uno strumento tecnico strettamente necessario: senza il token il server non saprebbe che sei tu e non potresti
        vedere preferiti e avvisi. Anche se non è tecnicamente un cookie ma un dato nel localStorage, lo trattiamo allo stesso
        modo e te lo dichiariamo qui.
      </p>
      <p>
        Chi naviga senza accedere ha salvata al massimo la preferenza del tema, se l'ha cambiata.
      </p>

      <h2>Come cancellarlo</h2>
      <ul>
        <li>Premi «Esci» nell'intestazione.</li>
        <li>Oppure cancella i dati del sito dalle impostazioni del browser.</li>
      </ul>

      <p>
        Per sapere quali dati conserviamo sul server leggi la <Link to="/privacy" className="underline">Privacy Policy</Link>.
      </p>
    </Informativa>
  )
}
