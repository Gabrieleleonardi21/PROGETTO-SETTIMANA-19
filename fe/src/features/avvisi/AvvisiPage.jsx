import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { QueryState } from '@/components/QueryState'
import { useAzione } from '@/hooks/useAzione'
import { eliminaAvviso, getAvvisi } from '@/lib/endpoints'
import { formatEuro } from '@/lib/format'

export function AvvisiPage() {
  const query = useQuery({ queryKey: ['avvisi'], queryFn: ({ signal }) => getAvvisi({ signal }) })
  const elimina = useAzione(eliminaAvviso, { invalida: ['avvisi'], successo: 'Avviso eliminato' })

  return (
    <div className="grid gap-6">
      <div>
        <h1 className="text-3xl font-semibold">Avvisi di prezzo</h1>
        <p className="mt-1 text-muted-foreground">
          Ogni avviso manda una sola mail, la prima volta che il prezzo scende sotto la soglia.
        </p>
      </div>
      <QueryState query={query} isEmpty={(d) => d.length === 0} empty="Nessun avviso. Aprilo dalla scheda di un'auto.">
        {(avvisi) => (
          <div className="rounded-xl border bg-card">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Auto</TableHead>
                  <TableHead className="text-right">Prezzo attuale</TableHead>
                  <TableHead className="text-right">Soglia</TableHead>
                  <TableHead>Stato</TableHead>
                  <TableHead className="sr-only">Azioni</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {avvisi.map((a) => (
                  <TableRow key={a.id}>
                    <TableCell>
                      {a.disponibile && (
                        <Link to={`/auto/${a.autoId}`} className="hover:underline">
                          {a.marca} {a.modello}
                        </Link>
                      )}
                      {!a.disponibile && <span className="text-muted-foreground">Auto non più disponibile</span>}
                    </TableCell>
                    <TableCell className="text-right tabular-nums">{formatEuro(a.prezzoAttuale)}</TableCell>
                    <TableCell className="text-right tabular-nums">{formatEuro(a.soglia)}</TableCell>
                    <TableCell>
                      {a.inviato && <Badge>Mail inviata</Badge>}
                      {!a.inviato && <Badge variant="secondary">In attesa</Badge>}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        aria-label={`Elimina avviso ${a.marca ?? ''} ${a.modello ?? ''}`}
                        disabled={elimina.isPending}
                        onClick={() => elimina.mutate(a.id)}
                      >
                        Elimina
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </QueryState>
    </div>
  )
}
