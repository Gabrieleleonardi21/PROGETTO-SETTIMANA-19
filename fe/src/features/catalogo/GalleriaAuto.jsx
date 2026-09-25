import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from '@/components/ui/carousel'
import { FotoAuto } from '@/components/FotoAuto'

// Il link ai crediti si mostra solo se è https (il backend accetta già solo siti ammessi: doppio controllo)
function linkSicuro(url) {
  if (typeof url === 'string' && url.startsWith('https://')) return url
  return null
}

/** Galleria del dettaglio: carosello se le foto sono più di una, altrimenti foto singola. Sotto, i crediti. */
export function GalleriaAuto({ auto }) {
  const nome = `${auto.marca} ${auto.modello}`
  const foto = auto.immagini ?? []
  const fonte = linkSicuro(auto.fonteFoto)

  let galleria = <FotoAuto src={foto[0]} alt={nome} priorita className="aspect-[16/10] w-full rounded-xl" />
  if (foto.length > 1) {
    galleria = (
      <Carousel opts={{ loop: true }} aria-label={`Foto di ${nome}`}>
        <CarouselContent>
          {foto.map((src, i) => (
            <CarouselItem key={src}>
              <FotoAuto
                src={src}
                alt={`${nome}, foto ${i + 1} di ${foto.length}`}
                priorita={i === 0}
                className="aspect-[16/10] w-full rounded-xl"
              />
            </CarouselItem>
          ))}
        </CarouselContent>
        {/* Frecce dentro la foto: fuori uscirebbero dallo schermo su mobile */}
        <CarouselPrevious className="left-3" />
        <CarouselNext className="right-3" />
      </Carousel>
    )
  }

  return (
    <figure className="grid gap-2">
      {galleria}
      {auto.creditiFoto && (
        <figcaption className="text-xs text-muted-foreground">
          {auto.creditiFoto}
          {fonte && (
            <>
              {' · '}
              <a href={fonte} target="_blank" rel="noopener noreferrer" className="underline underline-offset-2">
                fonte
              </a>
            </>
          )}
        </figcaption>
      )}
    </figure>
  )
}
