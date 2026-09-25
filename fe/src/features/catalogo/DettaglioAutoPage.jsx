import { useQuery } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useParams } from 'react-router'
import { ArrowLeft, Bell, Heart, HeartOff } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { FormField } from '@/components/FormField'
import { QueryState } from '@/components/QueryState'
import { useAzione } from '@/hooks/useAzione'
import { useAuth } from '@/lib/auth-context'
import * as endpoints from '@/lib/endpoints'
import { etichettaAlimentazione, formatEuro, formatKm } from '@/lib/format'
import { numero } from '@/lib/validazione'
import { GalleriaAuto } from './GalleriaAuto'

export function DettaglioAutoPage() {
  const { id } = useParams()
  const query = useQuery({ queryKey: ['auto', id], queryFn: ({ signal }) => endpoints.getAuto(id, { signal }) })

  return (
    <div className="grid gap-6">
      <Link to="/" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft aria-hidden="true" className="size-4" /> Torna al catalogo
      </Link>
      <QueryState query={query}>{(auto) => <Dettaglio auto={auto} />}</QueryState>
    </div>
  )
}

function Dettaglio({ auto }) {
  const { utente } = useAuth()
  return (
    <div className="grid gap-8 lg:grid-cols-[2fr_1fr]">
      <section className="grid content-start gap-4 rounded-xl border bg-card p-6">
        <GalleriaAuto auto={auto} />
        <h1 className="text-3xl font-semibold">
          {auto.marca} {auto.modello}
        </h1>
        <div className="flex flex-wrap gap-2">
          <Badge variant="secondary">Anno {auto.anno}</Badge>
          <Badge variant="secondary">{formatKm(auto.km)}</Badge>
          <Badge variant="outline">{etichettaAlimentazione(auto.alimentazione)}</Badge>
        </div>
        <p className="text-4xl font-semibold text-primary tabular-nums">{formatEuro(auto.prezzo)}</p>
        {/* La descrizione e' testo: React fa l'escape, niente dangerouslySetInnerHTML */}
        {auto.descrizione && <p className="whitespace-pre-line text-muted-foreground">{auto.descrizione}</p>}
      </section>

      <aside className="grid content-start gap-4">
        {utente && (
          <>
            <BottonePreferito autoId={auto.id} />
            <FormAvviso auto={auto} />
          </>
        )}
        {!utente && (
          <div className="rounded-xl border bg-card p-6 text-sm text-muted-foreground">
            <Link to="/accedi" className="text-primary underline underline-offset-4">Accedi</Link> per salvare l'auto tra i
            preferiti e ricevere una mail quando il prezzo scende.
          </div>
        )}
      </aside>
    </div>
  )
}

function BottonePreferito({ autoId }) {
  const preferiti = useQuery({ queryKey: ['preferiti'], queryFn: ({ signal }) => endpoints.getPreferiti({ signal }) })
  const esistente = preferiti.data?.find((p) => p.auto.id === autoId)
  const aggiungi = useAzione(() => endpoints.aggiungiPreferito(autoId), { invalida: ['preferiti'], successo: 'Aggiunta ai preferiti' })
  const rimuovi = useAzione(() => endpoints.rimuoviPreferito(esistente.id), { invalida: ['preferiti'], successo: 'Rimossa dai preferiti' })

  if (esistente) {
    return (
      <Button variant="outline" disabled={rimuovi.isPending} onClick={() => rimuovi.mutate()}>
        <HeartOff aria-hidden="true" /> Rimuovi dai preferiti
      </Button>
    )
  }
  return (
    <Button disabled={aggiungi.isPending || !preferiti.isSuccess} onClick={() => aggiungi.mutate()}>
      <Heart aria-hidden="true" /> Aggiungi ai preferiti
    </Button>
  )
}

function FormAvviso({ auto }) {
  const avvisi = useQuery({ queryKey: ['avvisi'], queryFn: ({ signal }) => endpoints.getAvvisi({ signal }) })
  const esistente = avvisi.data?.find((a) => a.autoId === auto.id)

  // La soglia deve stare sotto il prezzo attuale: lo stesso controllo lo fa AvvisoService
  const schema = z.object({
    soglia: numero('Inserisci un importo.', (n) =>
      n.positive('Deve essere positiva.').lt(auto.prezzo, 'Deve essere sotto il prezzo attuale.'),
    ),
  })
  const form = useForm({ resolver: zodResolver(schema), defaultValues: { soglia: '' } })
  const crea = useAzione((dati) => endpoints.creaAvviso({ autoId: auto.id, soglia: dati.soglia }), {
    invalida: ['avvisi'],
    successo: 'Avviso creato: ti scriveremo se il prezzo scende sotto la soglia',
    form,
  })

  return (
    <section className="grid gap-3 rounded-xl border bg-card p-6">
      <h2 className="flex items-center gap-2 text-lg font-semibold">
        <Bell aria-hidden="true" className="size-5 text-primary" /> Avviso di prezzo
      </h2>
      {esistente && (
        <p className="text-sm text-muted-foreground">
          Soglia impostata a <strong className="text-foreground">{formatEuro(esistente.soglia)}</strong>.{' '}
          <Link to="/avvisi" className="underline underline-offset-4">Gestisci i tuoi avvisi</Link>
        </p>
      )}
      {avvisi.isSuccess && !esistente && (
        <form noValidate onSubmit={form.handleSubmit((d) => crea.mutate(d))} className="grid gap-3">
          <FormField
            id="soglia"
            label="Avvisami sotto (€)"
            type="number"
            min="1"
            step="100"
            hint="Riceverai una sola mail, quando il prezzo scende sotto questa cifra."
            error={form.formState.errors.soglia}
            {...form.register('soglia')}
          />
          <Button type="submit" variant="secondary" disabled={crea.isPending}>
            Crea avviso
          </Button>
        </form>
      )}
    </section>
  )
}
