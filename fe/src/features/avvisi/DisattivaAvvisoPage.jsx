import { useMutation } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router'
import { Button } from '@/components/ui/button'
import { messaggioErrore } from '@/lib/errors'
import { disattivaAvviso } from '@/lib/endpoints'

/**
 * Pagina aperta dal link nella mail. Il token si invia solo quando l'utente preme il pulsante:
 * i client di posta aprono i link in anteprima, e un invio automatico consumerebbe il token monouso.
 */
export function DisattivaAvvisoPage() {
  const [params] = useSearchParams()
  const token = params.get('token') ?? ''
  const disattiva = useMutation({ mutationFn: () => disattivaAvviso(token) })

  let contenuto = (
    <>
      <p className="text-muted-foreground">Vuoi disattivare questo avviso di prezzo?</p>
      <Button className="mt-6" disabled={!token || disattiva.isPending} onClick={() => disattiva.mutate()}>
        Disattiva avviso
      </Button>
    </>
  )
  if (disattiva.isSuccess) {
    contenuto = <p className="text-muted-foreground">Avviso disattivato. Non riceverai altre mail per quest'auto.</p>
  }
  if (disattiva.isError) {
    contenuto = <p role="alert" className="text-destructive">{messaggioErrore(disattiva.error)}</p>
  }

  return (
    <div className="mx-auto max-w-md rounded-xl border bg-card p-8 text-center">
      <h1 className="text-2xl font-semibold">Disattiva avviso</h1>
      <div className="mt-4">{contenuto}</div>
      <Button asChild variant="link" className="mt-4">
        <Link to="/">Torna al catalogo</Link>
      </Button>
    </div>
  )
}
