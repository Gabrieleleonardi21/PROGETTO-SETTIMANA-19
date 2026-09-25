import { useState } from 'react'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { motion } from 'motion/react'
import { Search } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Pagination } from '@/components/Pagination'
import { QueryState } from '@/components/QueryState'
import { useDebouncedValue } from '@/hooks/useDebouncedValue'
import { getCatalogo } from '@/lib/endpoints'
import { AutoCard } from './AutoCard'
import { HeroSalone } from './HeroSalone'

// Stessi valori della whitelist ORDINAMENTI in AutoService: il BE rifiuta tutto il resto con 400
const ORDINAMENTI = [
  { value: 'recenti,desc', label: 'Più recenti' },
  { value: 'prezzo,asc', label: 'Prezzo crescente' },
  { value: 'prezzo,desc', label: 'Prezzo decrescente' },
  { value: 'anno,desc', label: 'Anno (più nuove)' },
  { value: 'km,asc', label: 'Meno km' },
  { value: 'marca,asc', label: 'Marca A-Z' },
]

// Le schede compaiono una dopo l'altra (con "riduci movimento" appaiono tutte subito)
const griglia = { nascosta: {}, visibile: { transition: { staggerChildren: 0.05 } } }
const scheda = { nascosta: { opacity: 0, y: 20 }, visibile: { opacity: 1, y: 0, transition: { duration: 0.35 } } }

export function CatalogoPage() {
  const [filtri, setFiltri] = useState({ q: '', prezzoMin: '', prezzoMax: '' })
  const [sort, setSort] = useState('recenti,desc')
  const [page, setPage] = useState(0)
  // Una richiesta quando si smette di scrivere, non a ogni tasto; ordinamento e pagina partono subito
  const cerca = useDebouncedValue(filtri)

  // Totale senza filtri per l'hero: basta un elemento per avere totalElements
  const totale = useQuery({
    queryKey: ['catalogo', 'totale'],
    queryFn: ({ signal }) => getCatalogo({ size: 1 }, { signal }),
  })

  const query = useQuery({
    queryKey: ['catalogo', cerca, sort, page],
    queryFn: ({ signal }) => getCatalogo({ ...cerca, sort, page, size: 12 }, { signal }),
    placeholderData: keepPreviousData,
  })

  // Ogni filtro nuovo riparte dalla prima pagina
  const aggiorna = (campo, valore) => {
    setFiltri((f) => ({ ...f, [campo]: valore }))
    setPage(0)
  }

  return (
    <div className="grid gap-6">
      <HeroSalone totale={totale.data?.totalElements ?? '…'} />
      <h2 className="text-2xl font-semibold">Le nostre auto</h2>

      <form role="search" onSubmit={(e) => e.preventDefault()} className="grid gap-4 rounded-xl border bg-card p-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="grid gap-1.5">
          <Label htmlFor="q">Marca o modello</Label>
          <div className="relative">
            <Search aria-hidden="true" className="absolute top-1/2 left-2.5 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input id="q" className="pl-8" value={filtri.q} maxLength={60} onChange={(e) => aggiorna('q', e.target.value)} />
          </div>
        </div>
        <div className="grid gap-1.5">
          <Label htmlFor="prezzoMin">Prezzo minimo (€)</Label>
          <Input id="prezzoMin" type="number" min="0" value={filtri.prezzoMin} onChange={(e) => aggiorna('prezzoMin', e.target.value)} />
        </div>
        <div className="grid gap-1.5">
          <Label htmlFor="prezzoMax">Prezzo massimo (€)</Label>
          <Input id="prezzoMax" type="number" min="0" value={filtri.prezzoMax} onChange={(e) => aggiorna('prezzoMax', e.target.value)} />
        </div>
        <div className="grid gap-1.5">
          <Label htmlFor="sort">Ordina per</Label>
          <Select
            value={sort}
            onValueChange={(v) => {
              setSort(v)
              setPage(0)
            }}
          >
            <SelectTrigger id="sort" className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {ORDINAMENTI.map((o) => (
                <SelectItem key={o.value} value={o.value}>
                  {o.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </form>

      <QueryState query={query} isEmpty={(d) => d.content.length === 0} empty="Nessuna auto corrisponde alla ricerca.">
        {(dati) => (
          <>
            <p className="text-sm text-muted-foreground">{dati.totalElements} auto trovate</p>
            {/* key sulla pagina e sui filtri: a ogni nuova ricerca le schede rientrano in sequenza */}
            <motion.div
              key={`${page}-${sort}-${JSON.stringify(cerca)}`}
              variants={griglia}
              initial="nascosta"
              animate="visibile"
              className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3"
            >
              {dati.content.map((auto) => (
                <motion.div key={auto.id} variants={scheda}>
                  <AutoCard auto={auto} />
                </motion.div>
              ))}
            </motion.div>
            <Pagination page={page} totalPages={dati.totalPages} onChange={setPage} />
          </>
        )}
      </QueryState>
    </div>
  )
}
