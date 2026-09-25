import { Link } from 'react-router'
import { motion, useMotionTemplate, useMotionValue, useReducedMotion, useSpring, useTransform } from 'motion/react'
import { Badge } from '@/components/ui/badge'
import { FotoAuto } from '@/components/FotoAuto'
import { etichettaAlimentazione, formatEuro, formatKm } from '@/lib/format'

// Inclinazione 3D e riflesso che segue il mouse: adattati da "Product Card" di ravikatiyar162 (21st.dev).
// Originale: https://21st.dev/@ravikatiyar162/components/product-card
// Qui le coordinate sono in proporzione alla card (0..1), così l'effetto va bene a ogni larghezza.
const MOLLA = { stiffness: 300, damping: 25 }

/** Scheda di un'auto nel catalogo e nei preferiti. `azioni` compare in fondo (es. "rimuovi"). */
export function AutoCard({ auto, azioni }) {
  const riduci = useReducedMotion()
  const x = useMotionValue(0.5)
  const y = useMotionValue(0.5)
  const rotateX = useSpring(useTransform(y, [0, 1], [6, -6]), MOLLA)
  const rotateY = useSpring(useTransform(x, [0, 1], [-6, 6]), MOLLA)
  const lucex = useTransform(x, [0, 1], [0, 100])
  const lucey = useTransform(y, [0, 1], [0, 100])
  const riflesso = useMotionTemplate`radial-gradient(220px at ${lucex}% ${lucey}%, color-mix(in oklch, var(--primary) 18%, transparent), transparent 70%)`

  const muovi = (e) => {
    if (riduci) return
    const r = e.currentTarget.getBoundingClientRect()
    x.set((e.clientX - r.left) / r.width)
    y.set((e.clientY - r.top) / r.height)
  }
  const esci = () => {
    x.set(0.5)
    y.set(0.5)
  }

  const nome = `${auto.marca} ${auto.modello}`

  return (
    <motion.article
      onMouseMove={muovi}
      onMouseLeave={esci}
      style={{ rotateX, rotateY, transformPerspective: 900 }}
      className="group relative flex flex-col overflow-hidden rounded-xl border bg-card shadow-sm transition-shadow hover:shadow-xl"
    >
      {/* Riflesso colorato sopra la card, non intercetta i click */}
      <motion.div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 z-10 opacity-0 transition-opacity duration-300 group-hover:opacity-100"
        style={{ background: riflesso }}
      />
      <div className="overflow-hidden">
        <FotoAuto
          src={auto.immagini?.[0]}
          alt={nome}
          crediti={auto.creditiFoto}
          className="aspect-[16/10] w-full transition-transform duration-500 group-hover:scale-105"
        />
      </div>
      <div className="flex flex-1 flex-col gap-2 p-4">
        <h2 className="text-lg font-semibold leading-tight">
          {/* Il link copre tutta la card (after:inset-0): si clicca ovunque, ma per lo screen reader è un link solo */}
          <Link to={`/auto/${auto.id}`} className="after:absolute after:inset-0 after:z-20 hover:underline">
            {nome}
          </Link>
        </h2>
        <div className="flex flex-wrap gap-1.5">
          <Badge variant="secondary">{auto.anno}</Badge>
          <Badge variant="secondary">{formatKm(auto.km)}</Badge>
          <Badge variant="outline">{etichettaAlimentazione(auto.alimentazione)}</Badge>
        </div>
        <p className="mt-auto pt-2 text-2xl font-semibold text-primary tabular-nums">{formatEuro(auto.prezzo)}</p>
        {/* Le azioni stanno sopra il link che copre la card, altrimenti non si potrebbero cliccare */}
        {azioni && <div className="relative z-30">{azioni}</div>}
      </div>
    </motion.article>
  )
}
