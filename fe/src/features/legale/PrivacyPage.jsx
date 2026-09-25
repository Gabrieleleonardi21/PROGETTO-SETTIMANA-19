import { Link } from 'react-router'
import { Informativa } from './Informativa'

// Descrive i dati che questa applicazione salva davvero (entità Utente, Preferito, Avviso)
export function PrivacyPage() {
  return (
    <Informativa titolo="Privacy Policy" aggiornata="25 settembre 2026">
      <p>
        Questa pagina spiega quali dati personali raccoglie Salone Auto, perché li usa, per quanto tempo li conserva e come
        puoi esercitare i tuoi diritti. Salone Auto è un progetto didattico.
      </p>

      <h2>Chi tratta i dati</h2>
      <p>Il titolare del trattamento è il gestore del sito Salone Auto, contattabile all'indirizzo email indicato nel sito.</p>

      <h2>Quali dati raccogliamo e perché</h2>
      <table className="w-full text-sm">
        <thead>
          <tr>
            <th>Dato</th>
            <th>A cosa serve</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Email</td>
            <td>Identificarti all'accesso e mandarti la mail dell'avviso di prezzo.</td>
          </tr>
          <tr>
            <td>Nome</td>
            <td>Salutarti nell'app e nella mail dell'avviso.</td>
          </tr>
          <tr>
            <td>Password</td>
            <td>Proteggere l'accesso. Salviamo solo un'impronta cifrata (BCrypt), mai la password in chiaro.</td>
          </tr>
          <tr>
            <td>Preferiti</td>
            <td>Mostrarti le auto che hai salvato.</td>
          </tr>
          <tr>
            <td>Avvisi e soglie di prezzo</td>
            <td>Mandarti una sola mail quando il prezzo di un'auto scende sotto la soglia che hai scelto.</td>
          </tr>
        </tbody>
      </table>
      <p>
        Non raccogliamo altro: niente telefono, indirizzo, dati di pagamento, geolocalizzazione o statistiche di navigazione.
        La base giuridica è l'esecuzione del servizio che hai chiesto registrandoti (art. 6.1.b GDPR).
      </p>

      <h2>Chi riceve i dati</h2>
      <ul>
        <li>Render (hosting di applicazione e database, server nell'Unione Europea, Francoforte).</li>
        <li>Google Gmail, solo per spedire la mail dell'avviso: riceve il tuo indirizzo, il nome e i dati dell'auto.</li>
        <li>
          Wikimedia Commons e DummyJSON, che ospitano le foto delle auto: quando una foto si carica, il loro server vede
          l'indirizzo IP del tuo dispositivo, come succede con qualsiasi immagine esterna. Non ricevono nessun altro
          dato e, grazie all'impostazione «no-referrer», nemmeno la pagina del salone da cui arriva la richiesta.
        </li>
      </ul>
      <p>Non vendiamo né cediamo i dati a nessun altro.</p>

      <h2>Per quanto li conserviamo</h2>
      <p>
        Finché il tuo account esiste. Un preferito o un avviso restano finché non li elimini tu. Quando elimini l'account
        cancelliamo subito account, preferiti e avvisi.
      </p>

      <h2>I tuoi diritti</h2>
      <ul>
        <li>Accesso e rettifica: dal tuo <Link to="/profilo" className="underline">profilo</Link> vedi i tuoi dati e cambi il nome.</li>
        <li>Cancellazione: dal profilo, «Elimina il mio account» cancella tutto e non parte più nessuna mail.</li>
        <li>Opposizione agli avvisi: puoi eliminare un avviso dalla pagina Avvisi o dal link nella mail.</li>
        <li>Puoi anche proporre reclamo al Garante per la protezione dei dati personali (garanteprivacy.it).</li>
      </ul>

      <p>
        Per i dati salvati nel browser leggi la <Link to="/cookie" className="underline">Cookie Policy</Link>.
      </p>
    </Informativa>
  )
}
