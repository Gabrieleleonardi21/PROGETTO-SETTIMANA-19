import { useState } from 'react'
import { CarFront } from 'lucide-react'
import { cn } from '@/lib/utils'

/**
 * Foto di un'auto con segnaposto se manca o non si carica.
 * referrerPolicy no-referrer: i siti delle foto non sanno da quale pagina del salone arriva la richiesta.
 * @param {{ src?: string, alt: string, className?: string, priorita?: boolean, crediti?: string }} props
 *   crediti: autore e licenza, mostrati come suggerimento al passaggio del mouse
 *   priorita=true per la foto principale del dettaglio (niente caricamento pigro)
 */
export function FotoAuto({ src, alt, className, priorita, crediti }) {
  const [rotta, setRotta] = useState(false)

  if (!src || rotta) {
    return (
      <div className={cn('flex items-center justify-center bg-secondary', className)}>
        <CarFront aria-hidden="true" className="size-14 text-muted-foreground/60" />
        {alt && <span className="sr-only">{alt}: foto non disponibile</span>}
      </div>
    )
  }

  let caricamento = 'lazy'
  if (priorita) caricamento = 'eager'

  return (
    <img
      src={src}
      alt={alt}
      loading={caricamento}
      decoding="async"
      referrerPolicy="no-referrer"
      title={crediti}
      onError={() => setRotta(true)}
      className={cn('bg-secondary object-cover', className)}
    />
  )
}
