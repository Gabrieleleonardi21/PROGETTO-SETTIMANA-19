import { useState } from 'react'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Pagination } from '@/components/Pagination'
import { QueryState } from '@/components/QueryState'
import { getAutoAdmin } from '@/lib/endpoints'
import { formatEuro, formatKm } from '@/lib/format'
import { FotoAuto } from '@/components/FotoAuto'
import { AutoDialog } from './AutoDialog'
import { PrezzoDialog } from './PrezzoDialog'

// Default export per il lazy() in App.jsx
export default function AdminAutoPage() {
  const [page, setPage] = useState(0)
  // Dialog aperto: { tipo: 'auto' | 'prezzo', auto?: object } oppure null
  const [dialog, setDialog] = useState(null)
  const query = useQuery({
    queryKey: ['admin-auto', page],
    queryFn: ({ signal }) => getAutoAdmin({ page, size: 20 }, { signal }),
    placeholderData: keepPreviousData,
  })

  const chiudi = (aperto) => {
    if (!aperto) setDialog(null)
  }

  return (
    <div className="grid gap-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-semibold">Gestione auto</h1>
          <p className="mt-1 text-muted-foreground">Tutte le auto, bozze comprese, con il prezzo d'acquisto.</p>
        </div>
        <Button onClick={() => setDialog({ tipo: 'auto' })}>
          <Plus aria-hidden="true" /> Nuova auto
        </Button>
      </div>

      <QueryState query={query} isEmpty={(d) => d.content.length === 0} empty="Nessuna auto: creane una.">
        {(dati) => (
          <>
            <div className="rounded-xl border bg-card">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Auto</TableHead>
                    <TableHead>Anno / km</TableHead>
                    <TableHead className="text-right">Acquisto</TableHead>
                    <TableHead className="text-right">Vendita</TableHead>
                    <TableHead>Stato</TableHead>
                    <TableHead className="sr-only">Azioni</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {dati.content.map((a) => (
                    <TableRow key={a.id}>
                      <TableCell className="font-medium">
                        <div className="flex items-center gap-3">
                          <FotoAuto src={a.immagini?.[0]} alt="" className="h-10 w-16 shrink-0 rounded-md" />
                          <span>
                            {a.marca} {a.modello}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {a.anno} · {formatKm(a.km)}
                      </TableCell>
                      <TableCell className="text-right tabular-nums">{formatEuro(a.prezzoAcquisto)}</TableCell>
                      <TableCell className="text-right font-medium tabular-nums">{formatEuro(a.prezzo)}</TableCell>
                      <TableCell>
                        {a.pubblicata && <Badge>Pubblicata</Badge>}
                        {!a.pubblicata && <Badge variant="outline">Bozza</Badge>}
                      </TableCell>
                      <TableCell className="text-right whitespace-nowrap">
                        <Button variant="ghost" size="sm" aria-label={`Cambia prezzo ${a.marca} ${a.modello}`} onClick={() => setDialog({ tipo: 'prezzo', auto: a })}>
                          Prezzo
                        </Button>
                        <Button variant="ghost" size="sm" aria-label={`Modifica ${a.marca} ${a.modello}`} onClick={() => setDialog({ tipo: 'auto', auto: a })}>
                          Modifica
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
            <Pagination page={page} totalPages={dati.totalPages} onChange={setPage} />
          </>
        )}
      </QueryState>

      <AutoDialog open={dialog?.tipo === 'auto'} onOpenChange={chiudi} auto={dialog?.auto} />
      <PrezzoDialog open={dialog?.tipo === 'prezzo'} onOpenChange={chiudi} auto={dialog?.auto} />
    </div>
  )
}
