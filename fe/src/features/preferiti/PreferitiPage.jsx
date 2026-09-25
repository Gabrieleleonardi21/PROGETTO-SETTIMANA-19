import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { Button } from '@/components/ui/button'
import { QueryState } from '@/components/QueryState'
import { useAzione } from '@/hooks/useAzione'
import { getPreferiti, rimuoviPreferito } from '@/lib/endpoints'
import { AutoCard } from '@/features/catalogo/AutoCard'

export function PreferitiPage() {
  const query = useQuery({ queryKey: ['preferiti'], queryFn: ({ signal }) => getPreferiti({ signal }) })
  const rimuovi = useAzione(rimuoviPreferito, { invalida: ['preferiti'], successo: 'Rimossa dai preferiti' })

  return (
    <div className="grid gap-6">
      <h1 className="text-3xl font-semibold">I miei preferiti</h1>
      <QueryState
        query={query}
        isEmpty={(d) => d.length === 0}
        empty={
          <>
            Non hai ancora preferiti. <Link to="/" className="text-primary underline underline-offset-4">Sfoglia il catalogo</Link>
          </>
        }
      >
        {(preferiti) => (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {preferiti.map((p) => (
              <AutoCard
                key={p.id}
                auto={p.auto}
                azioni={
                  <Button
                    variant="outline"
                    size="sm"
                    aria-label={`Rimuovi ${p.auto.marca} ${p.auto.modello} dai preferiti`}
                    disabled={rimuovi.isPending}
                    onClick={() => rimuovi.mutate(p.id)}
                  >
                    Rimuovi
                  </Button>
                }
              />
            ))}
          </div>
        )}
      </QueryState>
    </div>
  )
}
