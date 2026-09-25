import { Link, useRouteError } from 'react-router'
import { Button } from '@/components/ui/button'

function Messaggio({ titolo, testo }) {
  return (
    <div className="mx-auto max-w-xl px-4 py-24 text-center">
      <h1 className="text-3xl font-semibold">{titolo}</h1>
      <p className="mt-3 text-muted-foreground">{testo}</p>
      <Button asChild className="mt-8">
        <Link to="/">Torna al catalogo</Link>
      </Button>
    </div>
  )
}

/** Error boundary delle rotte: un errore di render non lascia la pagina bianca. */
export function ErrorPage() {
  const errore = useRouteError()
  // Il dettaglio serve a chi sviluppa, non all'utente
  if (import.meta.env.DEV) console.error(errore)
  return <Messaggio titolo="Qualcosa è andato storto" testo="Si è verificato un errore imprevisto. Riprova tra poco." />
}

export function NotFoundPage() {
  return <Messaggio titolo="Pagina non trovata" testo="La pagina che cerchi non esiste." />
}
