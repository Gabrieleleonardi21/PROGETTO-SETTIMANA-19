import { Informativa } from './Informativa'

// Fonti di dati, foto, codice e librerie usati nel progetto. Link esterni in una lista di dati,
// resi tutti dallo stesso componente: nuova scheda e niente accesso alla pagina di origine (noopener).
const SEZIONI = [
  {
    titolo: 'Dati delle auto',
    voci: [
      {
        nome: 'Auto di esempio',
        testo:
          'Marche e modelli sono reali, ma anno, chilometri, prezzi e descrizioni dei 18 modelli europei sono inventati per l\'esercizio. Nessuna auto è davvero in vendita.',
      },
      {
        nome: 'DummyJSON',
        url: 'https://dummyjson.com/docs/products',
        testo: 'API di dati fittizi per sviluppatori: 5 auto con prezzo e galleria di foto. Prezzi convertiti da dollari a euro in modo indicativo.',
      },
    ],
  },
  {
    titolo: 'Foto',
    voci: [
      {
        nome: 'Wikimedia Commons',
        url: 'https://commons.wikimedia.org',
        testo:
          'Foto dei 18 modelli europei, trovate con l\'API di Wikipedia. Ogni foto ha il suo autore e la sua licenza (CC BY-SA 3.0, CC BY-SA 4.0, GFDL), indicati sotto la foto nella scheda dell\'auto con il link alla pagina del file.',
      },
      {
        nome: 'API di Wikipedia',
        url: 'https://www.mediawiki.org/wiki/API:Main_page',
        testo: 'Per ogni modello: foto principale della voce inglese e, dall\'API di Commons, autore e licenza della foto.',
      },
      { nome: 'DummyJSON', url: 'https://dummyjson.com', testo: 'Foto delle 5 auto importate da DummyJSON.' },
    ],
  },
  {
    titolo: 'Codice di partenza e componenti',
    voci: [
      {
        nome: 'Template Deploy-Base-JSX',
        url: 'https://github.com/MatteoPattavinaDocente/JavaAIUnit5',
        testo: 'Scheletro BE + FE + PostgreSQL pronto per Render, dalla cartella HELP della repository del docente.',
      },
      {
        nome: 'Animated Theme Toggle, di johuniq (21st.dev)',
        url: 'https://21st.dev/@johuniq/components/animated-theme-toggle',
        testo: 'Pulsante del tema con il sole che diventa luna: convertito in JSX e adattato.',
      },
      {
        nome: 'Product Card, di ravikatiyar162 (21st.dev)',
        url: 'https://21st.dev/@ravikatiyar162/components/product-card',
        testo: 'Effetto di inclinazione 3D e riflesso che segue il mouse, riusato nelle schede delle auto.',
      },
      {
        nome: 'shadcn/ui',
        url: 'https://ui.shadcn.com',
        testo: 'Componenti di interfaccia (pulsanti, dialog, tabelle, carosello), basati su Radix UI.',
      },
    ],
  },
  {
    titolo: 'Librerie open source',
    voci: [
      { nome: 'Frontend', testo: 'React, React Router, TanStack Query, React Hook Form, Zod, Tailwind CSS, Radix UI, Motion, Embla Carousel, next-themes, Sonner, Lucide (icone), font Geist.' },
      { nome: '3D', url: 'https://threejs.org', testo: 'Three.js con React Three Fiber e Drei per l\'auto dell\'intestazione, costruita con forme semplici (nessun modello 3D esterno).' },
      { nome: 'Backend', testo: 'Spring Boot, Spring Security, Spring Data JPA con Hibernate, Thymeleaf (mail), Lombok, PostgreSQL.' },
    ],
  },
]

function Voce({ nome, url, testo }) {
  return (
    <li>
      <strong>
        {url && (
          <a href={url} target="_blank" rel="noopener noreferrer" className="underline underline-offset-2">
            {nome}
          </a>
        )}
        {!url && nome}
      </strong>
      {' — '}
      {testo}
    </li>
  )
}

export function CreditiPage() {
  return (
    <Informativa titolo="Crediti e fonti" aggiornata="25 settembre 2026">
      <p>Da dove arrivano dati, foto e parti di codice di questo progetto didattico.</p>
      {SEZIONI.map((sezione) => (
        <section key={sezione.titolo} className="grid gap-2">
          <h2>{sezione.titolo}</h2>
          <ul className="grid gap-2">
            {sezione.voci.map((voce) => (
              <Voce key={voce.nome} {...voce} />
            ))}
          </ul>
        </section>
      ))}
    </Informativa>
  )
}
